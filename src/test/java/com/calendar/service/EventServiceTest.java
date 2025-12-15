package com.calendar.service;

import com.calendar.model.Event;
import com.calendar.model.Reminder;
import com.calendar.model.User;
import com.calendar.repository.EventRepository;
import com.calendar.repository.ReminderRepository;
import com.calendar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit testy pre EventService.
 * 
 * <p>Tieto testy overujú správanie EventService bez potreby pripojenia k databáze.
 * Používajú Mockito na vytvorenie mock objektov pre repositories a ostatné závislosti.</p>
 * 
 * @author Andrej
 * @version 1.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventService Unit Tests")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private EventService eventService;

    private User testUser;
    private Event testEvent;

    /**
     * Pripraví testové dáta pred každým testom.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testEvent = new Event();
        testEvent.setId(1);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setStartTime(LocalDateTime.now().plusHours(1));
        testEvent.setEndTime(LocalDateTime.now().plusHours(2));
        testEvent.setUser(testUser);
        testEvent.setIsShared(false);
        testEvent.setIsAllDay(false);
        testEvent.setPriority("MEDIUM");
    }

    @Test
    @DisplayName("Vytvorenie udalosti - úspešné")
    void createEvent_Success() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        Event createdEvent = eventService.createEvent(testEvent, 1);

        // Assert
        assertNotNull(createdEvent);
        assertEquals("Test Event", createdEvent.getTitle());
        assertEquals(testUser, createdEvent.getUser());
        verify(userRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Vytvorenie udalosti - používateľ neexistuje")
    void createEvent_UserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventService.createEvent(testEvent, 999);
        });
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("Vytvorenie udalosti s pripomienkami")
    void createEvent_WithReminders() {
        // Arrange
        List<Integer> reminderMinutes = Arrays.asList(15, 60, 1440);
        testEvent.setReminderMinutes(reminderMinutes);
        
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(reminderRepository.findByEventIdAndUserId(anyInt(), anyInt())).thenReturn(new ArrayList<>());

        // Act
        Event createdEvent = eventService.createEvent(testEvent, 1);

        // Assert
        assertNotNull(createdEvent);
        verify(reminderRepository, times(3)).save(any(Reminder.class));
    }

    @Test
    @DisplayName("Získanie udalostí používateľa")
    void getUserEvents_Success() {
        // Arrange
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findByUserId(1)).thenReturn(events);
        when(reminderRepository.findByEventIdAndUserId(anyInt(), anyInt())).thenReturn(new ArrayList<>());

        // Act
        List<Event> userEvents = eventService.getUserEvents(1);

        // Assert
        assertNotNull(userEvents);
        assertEquals(1, userEvents.size());
        assertEquals("Test Event", userEvents.get(0).getTitle());
        verify(eventRepository, times(1)).findByUserId(1);
    }

    @Test
    @DisplayName("Získanie všetkých udalostí (vlastných + zdieľaných)")
    void getAllUserEvents_Success() {
        // Arrange
        List<Event> allEvents = Arrays.asList(testEvent);
        when(eventRepository.findByUserIdOrSharedWithUser(1)).thenReturn(allEvents);
        when(reminderRepository.findByEventIdAndUserId(anyInt(), anyInt())).thenReturn(new ArrayList<>());

        // Act
        List<Event> events = eventService.getAllUserEvents(1);

        // Assert
        assertNotNull(events);
        assertEquals(1, events.size());
        verify(eventRepository, times(1)).findByUserIdOrSharedWithUser(1);
    }

    @Test
    @DisplayName("Získanie udalostí v časovom rozsahu")
    void getUserEventsBetweenDates_Success() {
        // Arrange
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        List<Event> events = Arrays.asList(testEvent);
        
        when(eventRepository.findByUserIdAndStartTimeBetween(1, start, end)).thenReturn(events);
        when(reminderRepository.findByEventIdAndUserId(anyInt(), anyInt())).thenReturn(new ArrayList<>());

        // Act
        List<Event> filteredEvents = eventService.getUserEventsBetweenDates(1, start, end);

        // Assert
        assertNotNull(filteredEvents);
        assertEquals(1, filteredEvents.size());
        verify(eventRepository, times(1)).findByUserIdAndStartTimeBetween(1, start, end);
    }

    @Test
    @DisplayName("Aktualizácia udalosti - úspešná")
    void updateEvent_Success() {
        // Arrange
        Event updatedDetails = new Event();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setStartTime(testEvent.getStartTime());
        updatedDetails.setEndTime(testEvent.getEndTime());
        updatedDetails.setIsAllDay(false);
        updatedDetails.setPriority("HIGH");
        updatedDetails.setIsShared(false);
        
        when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        Event updated = eventService.updateEvent(1, updatedDetails);

        // Assert
        assertNotNull(updated);
        verify(eventRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(any(Event.class));
        verify(entityManager, times(1)).flush();
    }

    @Test
    @DisplayName("Aktualizácia udalosti - udalosť neexistuje")
    void updateEvent_EventNotFound() {
        // Arrange
        when(eventRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventService.updateEvent(999, testEvent);
        });
        
        assertTrue(exception.getMessage().contains("Event not found"));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("Vymazanie udalosti")
    void deleteEvent_Success() {
        // Arrange
        doNothing().when(eventRepository).deleteById(1);

        // Act
        eventService.deleteEvent(1);

        // Assert
        verify(eventRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Získanie zdieľaných udalostí")
    void getSharedEvents_Success() {
        // Arrange
        testEvent.setIsShared(true);
        List<Event> sharedEvents = Arrays.asList(testEvent);
        
        when(eventRepository.findSharedWithUser(1)).thenReturn(sharedEvents);
        when(reminderRepository.findByEventIdAndUserId(anyInt(), anyInt())).thenReturn(new ArrayList<>());

        // Act
        List<Event> events = eventService.getSharedEvents(1);

        // Assert
        assertNotNull(events);
        assertEquals(1, events.size());
        assertTrue(events.get(0).getIsShared());
        verify(eventRepository, times(1)).findSharedWithUser(1);
    }

    @Test
    @DisplayName("Získanie čakajúcich pripomienok")
    void getPendingReminders_Success() {
        // Arrange
        Reminder reminder1 = new Reminder();
        reminder1.setId(1);
        reminder1.setEvent(testEvent);
        reminder1.setUser(testUser);
        reminder1.setIsSent(false);
        
        List<Reminder> reminders = Arrays.asList(reminder1);
        when(reminderRepository.findPendingRemindersForUser(eq(1), any(LocalDateTime.class)))
            .thenReturn(reminders);

        // Act
        List<Reminder> pendingReminders = eventService.getPendingReminders(1);

        // Assert
        assertNotNull(pendingReminders);
        assertEquals(1, pendingReminders.size());
        assertFalse(pendingReminders.get(0).getIsSent());
        verify(reminderRepository, times(1)).findPendingRemindersForUser(eq(1), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Označenie pripomienky ako odoslanej")
    void markReminderAsSent_Success() {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setId(1);
        reminder.setIsSent(false);
        
        when(reminderRepository.findById(1)).thenReturn(Optional.of(reminder));
        when(reminderRepository.save(any(Reminder.class))).thenReturn(reminder);

        // Act
        eventService.markReminderAsSent(1);

        // Assert
        verify(reminderRepository, times(1)).findById(1);
        verify(reminderRepository, times(1)).save(any(Reminder.class));
    }

    @Test
    @DisplayName("Získanie oprávnení udalosti")
    void getEventPermissions_Success() {
        // Arrange
        List<Map<String, Object>> mockRows = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("userPermissions_KEY", 2);
        row1.put("permission", "VIEW");
        mockRows.add(row1);
        
        when(jdbcTemplate.queryForList(anyString(), anyInt())).thenReturn(mockRows);

        // Act
        Map<Integer, String> permissions = eventService.getEventPermissions(1);

        // Assert
        assertNotNull(permissions);
        assertEquals(1, permissions.size());
        assertEquals("VIEW", permissions.get(2));
    }

    @Test
    @DisplayName("Vymazanie pripomienky - admin operácia")
    void deleteReminder_Success() {
        // Arrange
        when(reminderRepository.existsById(1)).thenReturn(true);
        doNothing().when(reminderRepository).deleteById(1);

        // Act
        eventService.deleteReminder(1);

        // Assert
        verify(reminderRepository, times(1)).existsById(1);
        verify(reminderRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Vymazanie pripomienky - neexistuje")
    void deleteReminder_NotFound() {
        // Arrange
        when(reminderRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventService.deleteReminder(999);
        });
        
        assertTrue(exception.getMessage().contains("Reminder not found"));
        verify(reminderRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("Získanie všetkých pripomienok - admin operácia")
    void getAllReminders_Success() {
        // Arrange
        List<Reminder> allReminders = Arrays.asList(new Reminder(), new Reminder());
        when(reminderRepository.findAll()).thenReturn(allReminders);

        // Act
        List<Reminder> reminders = eventService.getAllReminders();

        // Assert
        assertNotNull(reminders);
        assertEquals(2, reminders.size());
        verify(reminderRepository, times(1)).findAll();
    }
}
