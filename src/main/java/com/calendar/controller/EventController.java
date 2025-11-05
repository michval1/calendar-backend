package com.calendar.controller;

import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.service.EventService;
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

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Event event, @RequestParam Integer userId) {
        try {
            Event createdEvent = eventService.createEvent(event, userId);
            return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to create event: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserEvents(@PathVariable Integer userId) {
        try {
            List<Event> events = eventService.getUserEvents(userId);
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch events: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<?> getAllUserEvents(@PathVariable Integer userId) {
        try {
            List<Event> events = eventService.getAllUserEvents(userId);
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch events: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<?> getUserEventsBetweenDates(
            @PathVariable Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<Event> events = eventService.getUserEventsBetweenDates(userId, start, end);
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch events: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}/all/range")
    public ResponseEntity<?> getAllUserEventsBetweenDates(
            @PathVariable Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<Event> events = eventService.getAllUserEventsBetweenDates(userId, start, end);
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch events: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable Integer eventId, @RequestBody Event event) {
        try {
            System.out.println("\n=== UPDATE EVENT REQUEST ===");
            System.out.println("Event ID: " + eventId);
            System.out.println("Event Data: " + event);
            System.out.println("UserPermissions from request: " + event.getUserPermissions());
            System.out.println("SharedWith from request: " + event.getSharedWith());
            System.out.println("===========================\n");

            Event updatedEvent = eventService.updateEvent(eventId, event);
            return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to update event: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Integer eventId) {
        try {

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

    @PostMapping("/{eventId}/share/{userId}")
    public ResponseEntity<?> shareEvent(
            @PathVariable Integer eventId,
            @PathVariable Integer userId) {
        System.out.println("\n" + "\"/{eventId}/share/{userId}\"" + eventId + " " + userId+"\n");

        try {
            Event sharedEvent = eventService.shareEvent(eventId, userId);
            return new ResponseEntity<>(sharedEvent, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to share event: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{eventId}/share/{userId}/permission")
    public ResponseEntity<?> shareEventWithPermission(
            @PathVariable Integer eventId,
            @PathVariable Integer userId,
            @RequestParam String permission) {
        try {
            Event sharedEvent = eventService.shareEvent(eventId, userId, permission);
            return new ResponseEntity<>(sharedEvent, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to share event: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{eventId}/share")
    public ResponseEntity<?> shareEventWithUsers(
            @PathVariable Integer eventId,
            @RequestBody List<Integer> userIds) {
        try {
            Event sharedEvent = eventService.shareEventWithUsers(eventId, userIds);
            return new ResponseEntity<>(sharedEvent, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to share event: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{eventId}/share/permissions")
    public ResponseEntity<?> shareEventWithPermissions(
            @PathVariable Integer eventId,
            @RequestBody Map<Integer, String> userPermissions) {
        try {
            Event sharedEvent = eventService.shareEventWithUsers(eventId, userPermissions);
            return new ResponseEntity<>(sharedEvent, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to share event: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{eventId}/share/{userId}")
    public ResponseEntity<?> removeSharedUser(
            @PathVariable Integer eventId,
            @PathVariable Integer userId) {
        try {
            Event updatedEvent = eventService.removeSharedUser(eventId, userId);
            return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to remove shared user: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/shared/{userId}")
    public ResponseEntity<?> getSharedEvents(@PathVariable Integer userId) {
        try {
            List<Event> events = eventService.getSharedEvents(userId);
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch shared events: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/shared/{userId}/range")
    public ResponseEntity<?> getSharedEventsBetweenDates(
            @PathVariable Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<Event> events = eventService.getSharedEventsBetweenDates(userId, start, end);
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch shared events: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{eventId}/shared-users")
    public ResponseEntity<?> getEventSharedUsers(@PathVariable Integer eventId) {
        try {
            Set<User> users = eventService.getEventSharedUsers(eventId);
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch shared users: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{eventId}/permissions")
    public ResponseEntity<?> getEventPermissions(@PathVariable Integer eventId) {
        try {
            Map<Integer, String> permissions = eventService.getEventPermissions(eventId);
            return new ResponseEntity<>(permissions, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch permissions: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{eventId}/with-permissions")
    public ResponseEntity<?> getEventWithPermissions(@PathVariable Integer eventId) {
        try {
            Map<String, Object> eventData = eventService.getEventWithPermissions(eventId);
            return new ResponseEntity<>(eventData, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch event: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{eventId}/user/{userId}/permission")
    public ResponseEntity<?> getUserPermission(
            @PathVariable Integer eventId,
            @PathVariable Integer userId) {
        try {
            String permission = eventService.getUserPermission(eventId, userId);
            Map<String, String> response = new HashMap<>();
            response.put("permission", permission);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch permission: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}