package com.calendar.controller;

import com.calendar.model.Event;
import com.calendar.model.Reminder;
import com.calendar.model.User;
import com.calendar.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Unit testy pre EventController.
 * 
 * <p>Testuje REST API endpointy pre správu udalostí a pripomienok.
 * Používa MockMvc na simuláciu HTTP requestov bez spustenia servera.</p>
 * 
 * @author Andrej
 * @version 1.0
 * @since 2024
 */
@WebMvcTest(EventController.class)
@DisplayName("EventController Unit Tests")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    private ObjectMapper objectMapper;
    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testEvent = new Event();
        testEvent.setId(1);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setStartTime(LocalDateTime.of(2024, 12, 20, 10, 0));
        testEvent.setEndTime(LocalDateTime.of(2024, 12, 20, 11, 0));
        testEvent.setUser(testUser);
        testEvent.setIsShared(false);
        testEvent.setIsAllDay(false);
        testEvent.setPriority("MEDIUM");
    }

    @Test
    @DisplayName("GET /api/events - získanie udalostí používateľa")
    void getAllEvents_Success() throws Exception {
        // Arrange
        List<Event> ownedEvents = Arrays.asList(testEvent);
        List<Event> sharedEvents = new ArrayList<>();
        
        when(eventService.getUserEvents(1)).thenReturn(ownedEvents);
        when(eventService.getSharedEvents(1)).thenReturn(sharedEvents);

        // Act & Assert
        mockMvc.perform(get("/api/events")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownedEvents", hasSize(1)))
                .andExpect(jsonPath("$.sharedEvents", hasSize(0)))
                .andExpect(jsonPath("$.ownedEvents[0].title", is("Test Event")));

        verify(eventService, times(1)).getUserEvents(1);
        verify(eventService, times(1)).getSharedEvents(1);
    }

    @Test
    @DisplayName("GET /api/events - s časovým rozsahom")
    void getAllEvents_WithDateRange() throws Exception {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        
        List<Event> ownedEvents = Arrays.asList(testEvent);
        List<Event> sharedEvents = new ArrayList<>();
        
        when(eventService.getUserEventsBetweenDates(eq(1), any(), any())).thenReturn(ownedEvents);
        when(eventService.getSharedEventsBetweenDates(eq(1), any(), any())).thenReturn(sharedEvents);

        // Act & Assert
        mockMvc.perform(get("/api/events")
                .param("userId", "1")
                .param("start", "2024-12-01T00:00:00")
                .param("end", "2024-12-31T23:59:59")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownedEvents", hasSize(1)));

        verify(eventService, times(1)).getUserEventsBetweenDates(eq(1), any(), any());
    }

    @Test
    @DisplayName("POST /api/events - vytvorenie udalosti")
    void createEvent_Success() throws Exception {
        // Arrange
        when(eventService.createEvent(any(Event.class), eq(1))).thenReturn(testEvent);

        // Act & Assert
        mockMvc.perform(post("/api/events")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Event")));

        verify(eventService, times(1)).createEvent(any(Event.class), eq(1));
    }

    @Test
    @DisplayName("POST /api/events - chyba pri vytváraní")
    void createEvent_Failure() throws Exception {
        // Arrange
        when(eventService.createEvent(any(Event.class), eq(1)))
            .thenThrow(new RuntimeException("Creation failed"));

        // Act & Assert
        mockMvc.perform(post("/api/events")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Failed to create event")));
    }

    @Test
    @DisplayName("PUT /api/events/{eventId} - aktualizácia udalosti")
    void updateEvent_Success() throws Exception {
        // Arrange
        Event updatedEvent = new Event();
        updatedEvent.setId(1);
        updatedEvent.setTitle("Updated Event");
        updatedEvent.setStartTime(testEvent.getStartTime());
        updatedEvent.setEndTime(testEvent.getEndTime());
        
        when(eventService.updateEvent(eq(1), any(Event.class))).thenReturn(updatedEvent);

        // Act & Assert
        mockMvc.perform(put("/api/events/1")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Event")));

        verify(eventService, times(1)).updateEvent(eq(1), any(Event.class));
    }

    @Test
    @DisplayName("PUT /api/events/{eventId} - chyba pri aktualizácii")
    void updateEvent_Failure() throws Exception {
        // Arrange
        when(eventService.updateEvent(eq(999), any(Event.class)))
            .thenThrow(new RuntimeException("Event not found"));

        // Act & Assert
        mockMvc.perform(put("/api/events/999")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Failed to update event")));
    }

    @Test
    @DisplayName("DELETE /api/events/{eventId} - vymazanie udalosti")
    void deleteEvent_Success() throws Exception {
        // Arrange
        doNothing().when(eventService).deleteEvent(1);

        // Act & Assert
        mockMvc.perform(delete("/api/events/1")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Event deleted successfully")));

        verify(eventService, times(1)).deleteEvent(1);
    }

    @Test
    @DisplayName("DELETE /api/events/{eventId} - chyba pri vymazávaní")
    void deleteEvent_Failure() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Delete failed")).when(eventService).deleteEvent(999);

        // Act & Assert
        mockMvc.perform(delete("/api/events/999")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("Failed to delete event")));
    }

    @Test
    @DisplayName("GET /api/events/reminders/pending - čakajúce pripomienky")
    void getPendingReminders_Success() throws Exception {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setId(1);
        reminder.setEvent(testEvent);
        reminder.setUser(testUser);
        reminder.setMinutesBeforeEvent(15);
        
        List<Reminder> reminders = Arrays.asList(reminder);
        when(eventService.getPendingReminders(1)).thenReturn(reminders);

        // Act & Assert
        mockMvc.perform(get("/api/events/reminders/pending")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(eventService, times(1)).getPendingReminders(1);
    }

    @Test
    @DisplayName("PUT /api/events/reminders/{reminderId}/mark-sent - označenie pripomienky")
    void markReminderAsSent_Success() throws Exception {
        // Arrange
        doNothing().when(eventService).markReminderAsSent(1);

        // Act & Assert
        mockMvc.perform(put("/api/events/reminders/1/mark-sent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Reminder marked as sent")));

        verify(eventService, times(1)).markReminderAsSent(1);
    }

    @Test
    @DisplayName("GET /api/events/reminders/all - všetky pripomienky (admin)")
    void getAllReminders_Success() throws Exception {
        // Arrange
        List<Reminder> allReminders = Arrays.asList(new Reminder(), new Reminder());
        when(eventService.getAllReminders()).thenReturn(allReminders);

        // Act & Assert
        mockMvc.perform(get("/api/events/reminders/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(eventService, times(1)).getAllReminders();
    }

    @Test
    @DisplayName("DELETE /api/events/reminders/{reminderId} - vymazanie pripomienky (admin)")
    void deleteReminder_Success() throws Exception {
        // Arrange
        doNothing().when(eventService).deleteReminder(1);

        // Act & Assert
        mockMvc.perform(delete("/api/events/reminders/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Reminder deleted successfully")));

        verify(eventService, times(1)).deleteReminder(1);
    }

    @Test
    @DisplayName("GET /api/events - chyba pri získavaní udalostí")
    void getAllEvents_ServiceError() throws Exception {
        // Arrange
        when(eventService.getUserEvents(1)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/events")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("Failed to fetch events")));
    }
}
