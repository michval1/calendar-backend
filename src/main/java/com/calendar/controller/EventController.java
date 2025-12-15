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

/**
 * REST Controller pre správu udalostí a pripomienok.
 *
 * <p>Poskytuje RESTful API endpointy pre:
 * <ul>
 *   <li>CRUD operácie s udalosťami</li>
 *   <li>Vyhľadávanie vlastných a zdieľaných udalostí</li>
 *   <li>Správu pripomienok</li>
 *   <li>Administrátorské funkcie</li>
 * </ul></p>
 *
 * <p>Všetky endpointy vracajú JSON odpovede a používajú HTTP status kódy
 * na indikáciu úspechu alebo chyby operácie.</p>
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.10:3000"})
@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * Vráti všetky udalosti používateľa rozdelené do dvoch kategórií.
     *
     * <p>Endpoint: GET /api/events?userId={userId}&start={start}&end={end}</p>
     *
     * <p>Vracia objekt s dvoma poľami:
     * <ul>
     *   <li>ownedEvents: udalosti vytvorené týmto používateľom</li>
     *   <li>sharedEvents: udalosti zdieľané s týmto používateľom (vytvorené inými)</li>
     * </ul></p>
     *
     * @param userId ID používateľa (povinný parameter)
     * @param start začiatok časového rozsahu (voliteľný, ISO 8601 formát)
     * @param end koniec časového rozsahu (voliteľný, ISO 8601 formát)
     * @return ResponseEntity s objektom obsahujúcim ownedEvents a sharedEvents
     *
     * @apiNote Ak sú uvedené start a end, vrátia sa len udalosti v danom rozsahu.
     *          Ak nie sú uvedené, vrátia sa všetky udalosti.
     *
     * @example GET /api/events?userId=1&start=2024-01-01T00:00:00&end=2024-01-31T23:59:59
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
     * Vytvorí novú udalosť pre používateľa.
     *
     * <p>Endpoint: POST /api/events?userId={userId}</p>
     *
     * <p>Request body musí obsahovať Event objekt so všetkými povinnými poľami.
     * Voliteľne môže obsahovať nastavenia zdieľania (sharedWith, userPermissions)
     * a pripomienky (reminderMinutes).</p>
     *
     * @param event objekt udalosti s údajmi (JSON v request body)
     * @param userId ID vlastníka udalosti (query parameter)
     * @return ResponseEntity s vytvorenou udalosťou (vrátane ID) alebo chybovou správou
     *
     * @apiNote Automaticky nastaví vlastníka udalosti podľa userId.
     *          Vráti HTTP 201 Created pri úspechu.
     *
     * @example POST /api/events?userId=1
     *          Body: {"title": "Meeting", "startTime": "2024-01-15T10:00:00", ...}
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
     * Aktualizuje existujúcu udalosť.
     *
     * <p>Endpoint: PUT /api/events/{eventId}?userId={userId}</p>
     *
     * <p>Aktualizuje všetky polia udalosti vrátane zdieľania a pripomienok.
     * Vyžaduje userId na overenie oprávnenia na úpravu udalosti.</p>
     *
     * @param eventId ID udalosti na aktualizáciu (path parameter)
     * @param event nové údaje udalosti (JSON v request body)
     * @param userId ID používateľa vykonávajúceho aktualizáciu (query parameter)
     * @return ResponseEntity s aktualizovanou udalosťou alebo chybovou správou
     *
     * @apiNote Používateľ musí byť vlastník alebo mať EDIT/ADMIN oprávnenie.
     *
     * @example PUT /api/events/123?userId=1
     *          Body: {"title": "Updated Meeting", "startTime": "2024-01-15T11:00:00", ...}
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
     * Vymaže udalosť.
     *
     * <p>Endpoint: DELETE /api/events/{eventId}?userId={userId}</p>
     *
     * <p>Vymaže udalosť vrátane všetkých spojených pripomienok a oprávnení.
     * Vyžaduje userId na overenie oprávnenia na vymazanie.</p>
     *
     * @param eventId ID udalosti na vymazanie (path parameter)
     * @param userId ID používateľa vykonávajúceho vymazanie (query parameter)
     * @return ResponseEntity s potvrdením alebo chybovou správou
     *
     * @apiNote Vymazať môže len vlastník alebo používateľ s ADMIN oprávnením.
     *
     * @example DELETE /api/events/123?userId=1
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
     * Vráti čakajúce pripomienky používateľa.
     *
     * <p>Endpoint: GET /api/events/reminders/pending?userId={userId}</p>
     *
     * <p>Vracia zoznam pripomienok, ktoré ešte neboli odoslané a ich čas
     * odoslania už nastal. Používa sa na zobrazenie notifikácií používateľovi.</p>
     *
     * @param userId ID používateľa (query parameter)
     * @return ResponseEntity so zoznamom čakajúcich pripomienok
     *
     * @apiNote Tento endpoint sa volá periodicky z frontend aplikácie.
     *
     * @example GET /api/events/reminders/pending?userId=1
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
     * Označí pripomienku ako odoslanú.
     *
     * <p>Endpoint: PUT /api/events/reminders/{reminderId}/mark-sent</p>
     *
     * <p>Používa sa po zobrazení notifikácie používateľovi, aby sa
     * pripomienka nezobrazila znova.</p>
     *
     * @param reminderId ID pripomienky (path parameter)
     * @return ResponseEntity s potvrdením alebo chybovou správou
     *
     * @example PUT /api/events/reminders/456/mark-sent
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
     * ADMIN ENDPOINT - Vráti všetky pripomienky v systéme.
     *
     * <p>Endpoint: GET /api/events/reminders/all</p>
     *
     * <p>Používa sa v admin paneli na prehľad všetkých pripomienok
     * všetkých používateľov.</p>
     *
     * @return ResponseEntity so zoznamom všetkých pripomienok
     *
     * @apiNote Tento endpoint by mal byť chránený autorizáciou na admin rolu.
     *
     * @example GET /api/events/reminders/all
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
     * ADMIN ENDPOINT - Vymaže pripomienku.
     *
     * <p>Endpoint: DELETE /api/events/reminders/{reminderId}</p>
     *
     * <p>Používa sa v admin paneli na manuálne vymazanie pripomienok.</p>
     *
     * @param reminderId ID pripomienky na vymazanie (path parameter)
     * @return ResponseEntity s potvrdením alebo chybovou správou
     *
     * @apiNote Tento endpoint by mal byť chránený autorizáciou na admin rolu.
     *
     * @example DELETE /api/events/reminders/456
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