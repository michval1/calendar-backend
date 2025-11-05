package com.calendar.service;

import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.repository.EventRepository;
import com.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Helper method to enrich events with permissions from the database
     */
    private void enrichEventsWithPermissions(List<Event> events) {
        for (Event event : events) {
            enrichEventWithPermissions(event);
        }
    }

    /**
     * Helper method to enrich a single event with permissions from the database
     */
    private void enrichEventWithPermissions(Event event) {
        if (event.getIsShared() && event.getId() != null) {
            Map<Integer, String> permissions = getEventPermissions(event.getId());
            event.setUserPermissions(permissions);
        } else {
            event.setUserPermissions(new HashMap<>());
        }
    }

    public Event createEvent(Event event, Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            event.setUser(userOptional.get());
            Event savedEvent = eventRepository.save(event);
            enrichEventWithPermissions(savedEvent);
            return savedEvent;
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    public List<Event> getUserEvents(Integer userId) {
        List<Event> events = eventRepository.findByUserId(userId);
        enrichEventsWithPermissions(events);
        return events;
    }

    public List<Event> getAllUserEvents(Integer userId) {
        List<Event> events = eventRepository.findByUserIdOrSharedWithUser(userId);
        enrichEventsWithPermissions(events);
        return events;
    }

    public List<Event> getUserEventsBetweenDates(Integer userId, LocalDateTime start, LocalDateTime end) {
        List<Event> events = eventRepository.findByUserIdAndStartTimeBetween(userId, start, end);
        enrichEventsWithPermissions(events);
        return events;
    }

    public List<Event> getAllUserEventsBetweenDates(Integer userId, LocalDateTime start, LocalDateTime end) {
        List<Event> events = eventRepository.findByUserIdOrSharedWithUserBetweenDates(userId, start, end);
        enrichEventsWithPermissions(events);
        return events;
    }

    @Transactional
    public Event updateEvent(Integer eventId, Event eventDetails) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isPresent()) {
            Event existingEvent = eventOptional.get();
            existingEvent.setTitle(eventDetails.getTitle());
            existingEvent.setDescription(eventDetails.getDescription());
            existingEvent.setLocation(eventDetails.getLocation());
            existingEvent.setStartTime(eventDetails.getStartTime());
            existingEvent.setEndTime(eventDetails.getEndTime());
            existingEvent.setIsAllDay(eventDetails.getIsAllDay());
            existingEvent.setRecurrenceType(eventDetails.getRecurrenceType());
            existingEvent.setRecurrenceEnd(eventDetails.getRecurrenceEnd());
            existingEvent.setPriority(eventDetails.getPriority());
            existingEvent.setColor(eventDetails.getColor());
            existingEvent.setIsShared(eventDetails.getIsShared());

            if (eventDetails.getSharedWith() != null) {
                existingEvent.setSharedWith(eventDetails.getSharedWith());
            }

            // Save the event first
            Event savedEvent = eventRepository.save(existingEvent);

            // IMPORTANT: Flush to ensure join table entries are created
            entityManager.flush();

            System.out.println("DEBUG Backend: Event saved and flushed. SharedWith count: " + savedEvent.getSharedWith().size());

            // Now handle the permissions update
            if (eventDetails.getUserPermissions() != null && !eventDetails.getUserPermissions().isEmpty()) {
                System.out.println("DEBUG Backend: Updating permissions for event " + eventId);
                System.out.println("DEBUG Backend: Permissions map: " + eventDetails.getUserPermissions());

                for (Map.Entry<Integer, String> entry : eventDetails.getUserPermissions().entrySet()) {
                    Integer userId = entry.getKey();
                    String permission = entry.getValue();

                    System.out.println("DEBUG Backend: Processing permission for user " + userId + " to " + permission);

                    // Check if the user is actually shared with this event
                    boolean userIsShared = savedEvent.getSharedWith().stream()
                            .anyMatch(user -> user.getId().equals(userId));

                    if (userIsShared) {
                        // Try to update first
                        String updateSql = "UPDATE event_shared_users SET permission = ? WHERE event_id = ? AND userPermissions_KEY = ?";
                        int rowsAffected = jdbcTemplate.update(updateSql, permission, eventId, userId);

                        System.out.println("DEBUG Backend: UPDATE affected " + rowsAffected + " rows for user " + userId);

                        // If no rows were updated, the entry might not exist yet (shouldn't happen after flush, but just in case)
                        if (rowsAffected == 0) {
                            System.out.println("DEBUG Backend: Row doesn't exist, attempting INSERT for user " + userId);
                            try {
                                String insertSql = "INSERT INTO event_shared_users (event_id, userPermissions_KEY, permission) VALUES (?, ?, ?)";
                                jdbcTemplate.update(insertSql, eventId, userId, permission);
                                System.out.println("DEBUG Backend: Successfully inserted permission for user " + userId);
                            } catch (Exception e) {
                                System.err.println("DEBUG Backend: Failed to insert permission: " + e.getMessage());
                                // If insert fails, try update again (race condition handling)
                                rowsAffected = jdbcTemplate.update(updateSql, permission, eventId, userId);
                                System.out.println("DEBUG Backend: Retry UPDATE affected " + rowsAffected + " rows");
                            }
                        }
                    } else {
                        System.out.println("DEBUG Backend: User " + userId + " is not in sharedWith list, skipping");
                    }
                }

                // Verify the final state
                System.out.println("DEBUG Backend: Verifying final permissions in DB...");
                String verifySql = "SELECT userPermissions_KEY, permission FROM event_shared_users WHERE event_id = ?";
                List<Map<String, Object>> dbPermissions = jdbcTemplate.queryForList(verifySql, eventId);
                System.out.println("DEBUG Backend: Current DB state: " + dbPermissions);

            } else {
                System.out.println("DEBUG Backend: No permissions to update");
            }

            // Enrich the event with permissions before returning
            enrichEventWithPermissions(savedEvent);
            return savedEvent;
        } else {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }
    }

    public void deleteEvent(Integer eventId) {
        eventRepository.deleteById(eventId);
    }

    @Transactional
    public Event shareEvent(Integer eventId, Integer userId, String permission) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (!eventOptional.isPresent()) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }

        if (!userOptional.isPresent()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        Event event = eventOptional.get();
        User user = userOptional.get();

        // Add user to the sharedWith collection
        event.shareWithUser(user);
        event = eventRepository.save(event);

        // Flush to ensure join table entry exists
        entityManager.flush();

        // Update permission in the join table
        String sql = "UPDATE event_shared_users SET permission = ? WHERE event_id = ? AND userPermissions_KEY = ?";
        jdbcTemplate.update(sql, permission, eventId, userId);

        // Enrich with permissions before returning
        enrichEventWithPermissions(event);
        return event;
    }

    // Original method for backward compatibility
    @Transactional
    public Event shareEvent(Integer eventId, Integer userId) {
        return shareEvent(eventId, userId, "VIEW");
    }

    @Transactional
    public Event shareEventWithUsers(Integer eventId, Map<Integer, String> userPermissions) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (!eventOptional.isPresent()) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }

        Event event = eventOptional.get();

        for (Map.Entry<Integer, String> entry : userPermissions.entrySet()) {
            Integer userId = entry.getKey();
            String permission = entry.getValue();

            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                event.shareWithUser(user);

                // We'll update permissions in bulk after saving
            }
        }

        // Save the event to update shared users
        event = eventRepository.save(event);

        // Flush to ensure join table entries exist
        entityManager.flush();

        // Now update all permissions in the join table
        for (Map.Entry<Integer, String> entry : userPermissions.entrySet()) {
            Integer userId = entry.getKey();
            String permission = entry.getValue();

            String sql = "UPDATE event_shared_users SET permission = ? WHERE event_id = ? AND userPermissions_KEY = ?";
            jdbcTemplate.update(sql, permission, eventId, userId);
        }

        // Enrich with permissions before returning
        enrichEventWithPermissions(event);
        return event;
    }

    // Original method for backward compatibility
    @Transactional
    public Event shareEventWithUsers(Integer eventId, List<Integer> userIds) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (!eventOptional.isPresent()) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }

        Event event = eventOptional.get();

        for (Integer userId : userIds) {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                event.shareWithUser(user);
            }
        }

        Event savedEvent = eventRepository.save(event);
        enrichEventWithPermissions(savedEvent);
        return savedEvent;
    }

    @Transactional
    public Event removeSharedUser(Integer eventId, Integer userId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (!eventOptional.isPresent()) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }

        if (!userOptional.isPresent()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        Event event = eventOptional.get();
        User user = userOptional.get();

        event.removeSharedUser(user);

        Event savedEvent = eventRepository.save(event);
        enrichEventWithPermissions(savedEvent);
        return savedEvent;
    }

    public List<Event> getSharedEvents(Integer userId) {
        List<Event> events = eventRepository.findSharedWithUser(userId);
        enrichEventsWithPermissions(events);
        return events;
    }

    public List<Event> getSharedEventsBetweenDates(Integer userId, LocalDateTime start, LocalDateTime end) {
        List<Event> events = eventRepository.findSharedWithUserBetweenDates(userId, start, end);
        enrichEventsWithPermissions(events);
        return events;
    }

    public Set<User> getEventSharedUsers(Integer eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (!eventOptional.isPresent()) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }

        Event event = eventOptional.get();
        return event.getSharedWith();
    }

    public String getUserPermission(Integer eventId, Integer userId) {
        try {
            String sql = "SELECT permission FROM event_shared_users WHERE event_id = ? AND userPermissions_KEY = ?";
            return jdbcTemplate.queryForObject(sql, String.class, eventId, userId);
        } catch (Exception e) {
            return "VIEW"; // Default permission if not found
        }
    }

    public Map<String, Object> getEventWithPermissions(Integer eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (!eventOpt.isPresent()) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }

        Event event = eventOpt.get();

        // Get all permissions for this event
        String sql = "SELECT userPermissions_KEY, permission FROM event_shared_users WHERE event_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, eventId);

        Map<Integer, String> permissions = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer userId = ((Number) row.get("userPermissions_KEY")).intValue();
            String permission = (String) row.get("permission");
            permissions.put(userId, permission);
        }

        // Enrich the event with permissions
        event.setUserPermissions(permissions);

        // Create a response with both event and permissions
        Map<String, Object> response = new HashMap<>();
        response.put("event", event);
        response.put("permissions", permissions);

        return response;
    }

    public Map<Integer, String> getEventPermissions(Integer eventId) {
        String sql = "SELECT userPermissions_KEY, permission FROM event_shared_users WHERE event_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, eventId);

        Map<Integer, String> permissions = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer userId = ((Number) row.get("userPermissions_KEY")).intValue();
            String permission = (String) row.get("permission");
            permissions.put(userId, permission);
        }

        return permissions;
    }
}