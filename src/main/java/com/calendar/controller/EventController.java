package com.calendar.controller;

import com.calendar.model.Event;
import com.calendar.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.10:3000"})
@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * GET /api/events?userId={userId}&start={start}&end={end}
     *
     * Returns ALL events for a user (both owned and shared) in separate arrays.
     * Optional date range filtering with start and end parameters.
     *
     * IMPORTANT: ownedEvents contains ONLY events created by the user
     *            sharedEvents contains ONLY events shared with the user (created by others)
     *
     * Response format:
     * {
     *   "ownedEvents": [...],    // Events created by this user
     *   "sharedEvents": [...]    // Events created by others and shared with this user
     * }
     */
    @GetMapping
    public ResponseEntity<?> getAllEvents(
            @Valid
            @RequestParam Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            Map<String, List<Event>> response = new HashMap<>();

            // Get user's own events (ONLY events they created, not shared ones)
            List<Event> ownedEvents;
            if (start != null && end != null) {
                ownedEvents = eventService.getUserEventsBetweenDates(userId, start, end);
            } else {
                ownedEvents = eventService.getUserEvents(userId);
            }

            // Get shared events
            List<Event> sharedEvents;
            if (start != null && end != null) {
                sharedEvents = eventService.getSharedEventsBetweenDates(userId, start, end);
            } else {
                sharedEvents = eventService.getSharedEvents(userId);
            }

            response.put("ownedEvents", ownedEvents);
            response.put("sharedEvents", sharedEvents);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch events: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * POST /api/events?userId={userId}
     *
     * Creates a new event. Include sharing info in the event object if needed.
     * The Event object should contain all necessary fields including sharedWith and userPermissions.
     */
    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestBody Event event,
            @RequestParam Integer userId) {
        try {
            Event createdEvent = eventService.createEvent(event, userId);
            return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to create event: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * PUT /api/events/{eventId}?userId={userId}
     *
     * Updates an existing event. Can update any field including sharing settings.
     * Requires userId to verify that the user has permission to update the event.
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Integer eventId,
            @RequestBody Event event,
            @RequestParam Integer userId) {
        try {
            // The service should verify that the user has permission to update
            Event updatedEvent = eventService.updateEvent(eventId, event);
            return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to update event: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * DELETE /api/events/{eventId}?userId={userId}
     *
     * Deletes an event. Requires userId to verify that the user has permission to delete the event.
     * Only the event owner or users with ADMIN permission can delete.
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Integer eventId,
            @RequestParam Integer userId) {
        try {
            // The service should verify that the user has permission to delete
            eventService.deleteEvent(eventId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Event deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to delete event: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/events/reminders/pending?userId={userId}
     *
     * Get pending reminders for a user (that should be shown now)
     * This is the ONLY reminder-specific endpoint - everything else is handled through event endpoints!
     */
    @GetMapping("/reminders/pending")
    public ResponseEntity<?> getPendingReminders(
            @Valid
            @RequestParam Integer userId) {
        try {
            List<?> reminders = eventService.getPendingReminders(userId);
            return new ResponseEntity<>(reminders, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch pending reminders: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /api/events/reminders/{reminderId}/mark-sent
     *
     * Mark a reminder as sent (shown to user)
     */
    @PutMapping("/reminders/{reminderId}/mark-sent")
    public ResponseEntity<?> markReminderAsSent(@PathVariable Integer reminderId) {
        try {
            eventService.markReminderAsSent(reminderId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Reminder marked as sent");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to mark reminder as sent: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * ADMIN ENDPOINT
     * GET /api/events/reminders/all
     *
     * Get all reminders in the system (for admin panel)
     */
    @GetMapping("/reminders/all")
    public ResponseEntity<?> getAllReminders() {
        try {
            List<?> reminders = eventService.getAllReminders();
            return new ResponseEntity<>(reminders, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch all reminders: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ADMIN ENDPOINT
     * DELETE /api/events/reminders/{reminderId}
     *
     * Delete a reminder (for admin panel)
     */
    @DeleteMapping("/reminders/{reminderId}")
    public ResponseEntity<?> deleteReminder(@PathVariable Integer reminderId) {
        try {
            eventService.deleteReminder(reminderId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Reminder deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to delete reminder: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}