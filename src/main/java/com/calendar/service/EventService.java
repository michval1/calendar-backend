package com.calendar.service;

import com.calendar.model.Event;
import com.calendar.model.Reminder;
import com.calendar.model.User;
import com.calendar.repository.EventRepository;
import com.calendar.repository.ReminderRepository;
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
    private ReminderRepository reminderRepository;

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

    /**
     * Helper method to load reminder minutes for an event and user
     */
    private void loadReminderMinutes(Event event, Integer userId) {
        if (event.getId() != null && userId != null) {
            List<Reminder> reminders = reminderRepository.findByEventIdAndUserId(event.getId(), userId);
            List<Integer> minutes = new ArrayList<>();
            for (Reminder reminder : reminders) {
                minutes.add(reminder.getMinutesBeforeEvent());
            }
            event.setReminderMinutes(minutes);
        }
    }

    /**
     * Helper method to load reminder minutes for multiple events
     */
    private void loadReminderMinutesForEvents(List<Event> events, Integer userId) {
        for (Event event : events) {
            loadReminderMinutes(event, userId);
        }
    }

    /**
     * Helper method to save reminders for an event
     */
    @Transactional
    private void saveReminders(Event event, Integer userId, List<Integer> minutesList) {
        if (minutesList == null || minutesList.isEmpty()) {
            return;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return;
        }

        User user = userOpt.get();

        // Delete existing reminders for this event and user
        List<Reminder> existingReminders = reminderRepository.findByEventIdAndUserId(event.getId(), userId);
        reminderRepository.deleteAll(existingReminders);

        // Create new reminders
        for (Integer minutesBefore : minutesList) {
            Reminder reminder = new Reminder(event, user, minutesBefore);
            reminderRepository.save(reminder);
        }
    }

    @Transactional
    public Event createEvent(Event event, Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            event.setUser(userOptional.get());
            Event savedEvent = eventRepository.save(event);
            enrichEventWithPermissions(savedEvent);

            // Save reminders if provided
            if (event.getReminderMinutes() != null && !event.getReminderMinutes().isEmpty()) {
                saveReminders(savedEvent, userId, event.getReminderMinutes());
                savedEvent.setReminderMinutes(event.getReminderMinutes());
            }

            return savedEvent;
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    public List<Event> getUserEvents(Integer userId) {
        List<Event> events = eventRepository.findByUserId(userId);
        enrichEventsWithPermissions(events);
        loadReminderMinutesForEvents(events, userId);
        return events;
    }

    public List<Event> getAllUserEvents(Integer userId) {
        List<Event> events = eventRepository.findByUserIdOrSharedWithUser(userId);
        enrichEventsWithPermissions(events);
        loadReminderMinutesForEvents(events, userId);
        return events;
    }

    public List<Event> getUserEventsBetweenDates(Integer userId, LocalDateTime start, LocalDateTime end) {
        List<Event> events = eventRepository.findByUserIdAndStartTimeBetween(userId, start, end);
        enrichEventsWithPermissions(events);
        loadReminderMinutesForEvents(events, userId);
        return events;
    }

    public List<Event> getAllUserEventsBetweenDates(Integer userId, LocalDateTime start, LocalDateTime end) {
        List<Event> events = eventRepository.findByUserIdOrSharedWithUserBetweenDates(userId, start, end);
        enrichEventsWithPermissions(events);
        loadReminderMinutesForEvents(events, userId);
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

            // Flush to ensure join table entries are created
            entityManager.flush();

            // Handle permissions update
            if (eventDetails.getUserPermissions() != null && !eventDetails.getUserPermissions().isEmpty()) {
                for (Map.Entry<Integer, String> entry : eventDetails.getUserPermissions().entrySet()) {
                    Integer userId = entry.getKey();
                    String permission = entry.getValue();

                    boolean userIsShared = savedEvent.getSharedWith().stream()
                            .anyMatch(user -> user.getId().equals(userId));

                    if (userIsShared) {
                        String updateSql = "UPDATE event_shared_users SET permission = ? WHERE event_id = ? AND userPermissions_KEY = ?";
                        int rowsAffected = jdbcTemplate.update(updateSql, permission, eventId, userId);

                        if (rowsAffected == 0) {
                            try {
                                String insertSql = "INSERT INTO event_shared_users (event_id, userPermissions_KEY, permission) VALUES (?, ?, ?)";
                                jdbcTemplate.update(insertSql, eventId, userId, permission);
                            } catch (Exception e) {
                                rowsAffected = jdbcTemplate.update(updateSql, permission, eventId, userId);
                            }
                        }
                    }
                }
            }

            // Handle reminders update (only for the event owner)
            Integer ownerId = savedEvent.getUser().getId();
            if (eventDetails.getReminderMinutes() != null) {
                saveReminders(savedEvent, ownerId, eventDetails.getReminderMinutes());
                savedEvent.setReminderMinutes(eventDetails.getReminderMinutes());
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

    public List<Event> getSharedEvents(Integer userId) {
        List<Event> events = eventRepository.findSharedWithUser(userId);
        enrichEventsWithPermissions(events);
        loadReminderMinutesForEvents(events, userId);
        return events;
    }

    public List<Event> getSharedEventsBetweenDates(Integer userId, LocalDateTime start, LocalDateTime end) {
        List<Event> events = eventRepository.findSharedWithUserBetweenDates(userId, start, end);
        enrichEventsWithPermissions(events);
        loadReminderMinutesForEvents(events, userId);
        return events;
    }

    /**
     * Get pending reminders for a user (should be shown now)
     */
    public List<Reminder> getPendingReminders(Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        return reminderRepository.findPendingRemindersForUser(userId, now);
    }

    /**
     * Mark a reminder as sent
     */
    @Transactional
    public void markReminderAsSent(Integer reminderId) {
        Optional<Reminder> reminderOpt = reminderRepository.findById(reminderId);
        if (reminderOpt.isPresent()) {
            Reminder reminder = reminderOpt.get();
            reminder.setIsSent(true);
            reminder.setSentAt(LocalDateTime.now());
            reminderRepository.save(reminder);
        }
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

    // ADMIN METHODS

    /**
     * Get all reminders in the system (for admin panel)
     */
    public List<Reminder> getAllReminders() {
        return reminderRepository.findAll();
    }

    /**
     * Delete a reminder by ID (for admin panel)
     */
    @Transactional
    public void deleteReminder(Integer reminderId) {
        if (!reminderRepository.existsById(reminderId)) {
            throw new RuntimeException("Reminder not found with ID: " + reminderId);
        }
        reminderRepository.deleteById(reminderId);
    }
}