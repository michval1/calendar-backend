package com.calendar.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testy pre Event model.
 * 
 * <p>Testuje gettery, settery a business logiku Event entity.</p>
 * 
 * @author Andrej
 * @version 1.0
 * @since 2024
 */
@DisplayName("Event Model Unit Tests")
class EventTest {

    private Event event;
    private User testUser;

    @BeforeEach
    void setUp() {
        event = new Event();
        
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    @DisplayName("Vytvorenie prázdnej udalosti")
    void createEmptyEvent() {
        // Assert
        assertNotNull(event);
        assertNull(event.getId());
        assertNull(event.getTitle());
    }

    @Test
    @DisplayName("Nastavenie a získanie základných polí")
    void setAndGetBasicFields() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 12, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 20, 11, 0);

        // Act
        event.setId(1);
        event.setTitle("Test Event");
        event.setDescription("Test Description");
        event.setLocation("Test Location");
        event.setStartTime(start);
        event.setEndTime(end);
        event.setIsAllDay(false);
        event.setPriority("HIGH");
        event.setColor("#FF5733");

        // Assert
        assertEquals(1, event.getId());
        assertEquals("Test Event", event.getTitle());
        assertEquals("Test Description", event.getDescription());
        assertEquals("Test Location", event.getLocation());
        assertEquals(start, event.getStartTime());
        assertEquals(end, event.getEndTime());
        assertFalse(event.getIsAllDay());
        assertEquals("HIGH", event.getPriority());
        assertEquals("#FF5733", event.getColor());
    }

    @Test
    @DisplayName("Nastavenie a získanie opakovania")
    void setAndGetRecurrence() {
        // Arrange
        LocalDateTime recurrenceEnd = LocalDateTime.of(2025, 12, 20, 10, 0);

        // Act
        event.setRecurrenceType("weekly");
        event.setRecurrenceEnd(recurrenceEnd);

        // Assert
        assertEquals("weekly", event.getRecurrenceType());
        assertEquals(recurrenceEnd, event.getRecurrenceEnd());
    }

    @Test
    @DisplayName("Nastavenie a získanie používateľa")
    void setAndGetUser() {
        // Act
        event.setUser(testUser);

        // Assert
        assertNotNull(event.getUser());
        assertEquals(1, event.getUser().getId());
        assertEquals("testuser", event.getUser().getUsername());
    }

    @Test
    @DisplayName("Predvolené hodnoty")
    void defaultValues() {
        // Assert
        assertFalse(event.getIsAllDay());
        assertFalse(event.getIsShared());
        assertEquals("MEDIUM", event.getPriority());
    }

    @Test
    @DisplayName("Zdieľanie udalosti s používateľom")
    void shareWithUser() {
        // Arrange
        User sharedUser = new User();
        sharedUser.setId(2);
        sharedUser.setUsername("shareduser");

        // Act
        event.shareWithUser(sharedUser);

        // Assert
        assertTrue(event.getIsShared());
        assertTrue(event.getSharedWith().contains(sharedUser));
        assertEquals(1, event.getSharedWith().size());
    }

    @Test
    @DisplayName("Zdieľanie s viacerými používateľmi")
    void shareWithMultipleUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(2);
        User user2 = new User();
        user2.setId(3);

        // Act
        event.shareWithUser(user1);
        event.shareWithUser(user2);

        // Assert
        assertTrue(event.getIsShared());
        assertEquals(2, event.getSharedWith().size());
    }

    @Test
    @DisplayName("Odstránenie zdieľaného používateľa")
    void removeSharedUser() {
        // Arrange
        User sharedUser = new User();
        sharedUser.setId(2);
        event.shareWithUser(sharedUser);

        // Act
        event.removeSharedUser(sharedUser);

        // Assert
        assertFalse(event.getIsShared());
        assertTrue(event.getSharedWith().isEmpty());
    }

    @Test
    @DisplayName("Odstránenie jedného zo zdieľaných používateľov")
    void removeOneOfMultipleSharedUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(2);
        User user2 = new User();
        user2.setId(3);
        
        event.shareWithUser(user1);
        event.shareWithUser(user2);

        // Act
        event.removeSharedUser(user1);

        // Assert
        assertTrue(event.getIsShared(), "Udalosť by mala ostať zdieľaná");
        assertEquals(1, event.getSharedWith().size());
        assertTrue(event.getSharedWith().contains(user2));
        assertFalse(event.getSharedWith().contains(user1));
    }

    @Test
    @DisplayName("Nastavenie oprávnení používateľov")
    void setUserPermissions() {
        // Arrange
        Map<Integer, String> permissions = new HashMap<>();
        permissions.put(2, "VIEW");
        permissions.put(3, "EDIT");
        permissions.put(4, "ADMIN");

        // Act
        event.setUserPermissions(permissions);

        // Assert
        assertEquals(3, event.getUserPermissions().size());
        assertEquals("VIEW", event.getUserPermissions().get(2));
        assertEquals("EDIT", event.getUserPermissions().get(3));
        assertEquals("ADMIN", event.getUserPermissions().get(4));
    }

    @Test
    @DisplayName("Nastavenie pripomienok v minútach")
    void setReminderMinutes() {
        // Arrange
        List<Integer> reminders = Arrays.asList(15, 60, 1440);

        // Act
        event.setReminderMinutes(reminders);

        // Assert
        assertEquals(3, event.getReminderMinutes().size());
        assertTrue(event.getReminderMinutes().contains(15));
        assertTrue(event.getReminderMinutes().contains(60));
        assertTrue(event.getReminderMinutes().contains(1440));
    }

    @Test
    @DisplayName("Prázdny zoznam zdieľaných používateľov")
    void emptySharedWithList() {
        // Assert
        assertNotNull(event.getSharedWith());
        assertTrue(event.getSharedWith().isEmpty());
    }

    @Test
    @DisplayName("Prázdna mapa oprávnení")
    void emptyPermissionsMap() {
        // Assert
        assertNotNull(event.getUserPermissions());
        assertTrue(event.getUserPermissions().isEmpty());
    }

    @Test
    @DisplayName("Prázdny zoznam pripomienok")
    void emptyRemindersList() {
        // Assert
        assertNotNull(event.getReminderMinutes());
        assertTrue(event.getReminderMinutes().isEmpty());
    }

    @Test
    @DisplayName("Nastavenie všetkých polí cez konštruktor")
    void allArgsConstructor() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 12, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 20, 11, 0);
        Set<User> sharedUsers = new HashSet<>();
        Map<Integer, String> permissions = new HashMap<>();
        List<Integer> reminders = new ArrayList<>();

        // Act
        Event fullEvent = new Event(
            1, "Title", "Description", "Location",
            start, end, false, "weekly", null,
            testUser, "HIGH", "#FF5733", true,
            sharedUsers, permissions, reminders
        );

        // Assert
        assertEquals(1, fullEvent.getId());
        assertEquals("Title", fullEvent.getTitle());
        assertEquals("Description", fullEvent.getDescription());
        assertEquals("Location", fullEvent.getLocation());
        assertEquals(start, fullEvent.getStartTime());
        assertEquals(end, fullEvent.getEndTime());
        assertFalse(fullEvent.getIsAllDay());
        assertEquals("weekly", fullEvent.getRecurrenceType());
        assertEquals(testUser, fullEvent.getUser());
        assertEquals("HIGH", fullEvent.getPriority());
        assertEquals("#FF5733", fullEvent.getColor());
        assertTrue(fullEvent.getIsShared());
    }
}
