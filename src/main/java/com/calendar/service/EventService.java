package com.calendar.service;

import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.repository.EventRepository;
import com.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Event createEvent(Event event, Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            event.setUser(userOptional.get());
            return eventRepository.save(event);
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    public List<Event> getUserEvents(Integer userId) {
        return eventRepository.findByUserId(userId);
    }

    public List<Event> getAllUserEvents(Integer userId) {
        return eventRepository.findByUserIdOrSharedWithUser(userId);
    }

    public List<Event> getUserEventsBetweenDates(Integer userId, LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByUserIdAndStartTimeBetween(userId, start, end);
    }

    public List<Event> getAllUserEventsBetweenDates(Integer userId, LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByUserIdOrSharedWithUserBetweenDates(userId, start, end);
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

            return eventRepository.save(existingEvent);
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

        // Update permission in the join table
        String sql = "UPDATE event_shared_users SET permission = ? WHERE event_id = ? AND userPermissions_KEY = ?";
        jdbcTemplate.update(sql, permission, eventId, userId);

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

        // Now update all permissions in the join table
        for (Map.Entry<Integer, String> entry : userPermissions.entrySet()) {
            Integer userId = entry.getKey();
            String permission = entry.getValue();

            String sql = "UPDATE event_shared_users SET permission = ? WHERE event_id = ? AND userPermissions_KEY = ?";
            jdbcTemplate.update(sql, permission, eventId, userId);
        }

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

        return eventRepository.save(event);
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

        return eventRepository.save(event);
    }

    public List<Event> getSharedEvents(Integer userId) {
        return eventRepository.findSharedWithUser(userId);
    }

    public List<Event> getSharedEventsBetweenDates(Integer userId, LocalDateTime start, LocalDateTime end) {
        return eventRepository.findSharedWithUserBetweenDates(userId, start, end);
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