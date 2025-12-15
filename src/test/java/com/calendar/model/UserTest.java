package com.calendar.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testy pre User model.
 * 
 * <p>Testuje gettery, settery a základnú funkčnosť User entity.</p>
 *
 */
@DisplayName("User Model Unit Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    @DisplayName("Vytvorenie prázdneho používateľa")
    void createEmptyUser() {
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
    }

    @Test
    @DisplayName("Nastavenie a získanie ID")
    void setAndGetId() {
        // Act
        user.setId(1);

        assertEquals(1, user.getId());
    }

    @Test
    @DisplayName("Nastavenie a získanie používateľského mena")
    void setAndGetUsername() {
        // Act
        user.setUsername("testuser");

        assertEquals("testuser", user.getUsername());
    }

    @Test
    @DisplayName("Nastavenie a získanie emailu")
    void setAndGetEmail() {
        // Act
        user.setEmail("test@example.com");

        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Nastavenie všetkých polí")
    void setAllFields() {
        // Act
        user.setId(1);
        user.setUsername("john_doe");
        user.setEmail("john.doe@example.com");

        assertEquals(1, user.getId());
        assertEquals("john_doe", user.getUsername());
        assertEquals("john.doe@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Vytvorenie používateľa cez konštruktor")
    void allArgsConstructor() {
        // Act
        User newUser = new User(2, "jane_doe", "jane@example.com");

        assertEquals(2, newUser.getId());
        assertEquals("jane_doe", newUser.getUsername());
        assertEquals("jane@example.com", newUser.getEmail());
    }

    @Test
    @DisplayName("Nastavenie null hodnôt")
    void setNullValues() {
        // Arrange
        user.setId(1);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        // Act
        user.setId(null);
        user.setUsername(null);
        user.setEmail(null);

        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
    }

    @Test
    @DisplayName("Nastavenie prázdneho username")
    void setEmptyUsername() {
        // Act
        user.setUsername("");

        assertEquals("", user.getUsername());
    }

    @Test
    @DisplayName("Nastavenie prázdneho emailu")
    void setEmptyEmail() {
        // Act
        user.setEmail("");

        assertEquals("", user.getEmail());
    }

    @Test
    @DisplayName("Overenie referenčnej rovnosti")
    void referenceEquality() {
        // Arrange
        user.setId(1);
        user.setUsername("testuser");
        User sameUser = user;

        assertSame(user, sameUser);
    }

    @Test
    @DisplayName("Overenie rozdielnych inštancií")
    void differentInstances() {
        // Arrange
        User user1 = new User(1, "user1", "user1@example.com");
        User user2 = new User(2, "user2", "user2@example.com");

        assertNotSame(user1, user2);
    }

    @Test
    @DisplayName("Username s medzeram")
    void usernameWithSpaces() {
        // Act
        user.setUsername("user with spaces");

        assertEquals("user with spaces", user.getUsername());
    }

    @Test
    @DisplayName("Email s veľkými písmenami")
    void emailWithUpperCase() {
        // Act
        user.setEmail("Test@EXAMPLE.COM");

        assertEquals("Test@EXAMPLE.COM", user.getEmail());
    }

    @Test
    @DisplayName("Dlhé používateľské meno")
    void longUsername() {
        // Arrange
        String longUsername = "a".repeat(50);

        // Act
        user.setUsername(longUsername);

        assertEquals(50, user.getUsername().length());
        assertEquals(longUsername, user.getUsername());
    }

    @Test
    @DisplayName("Dlhý email")
    void longEmail() {
        // Arrange
        String longEmail = "a".repeat(90) + "@example.com";

        // Act
        user.setEmail(longEmail);

        assertEquals(longEmail, user.getEmail());
    }

    @Test
    @DisplayName("Špeciálne znaky v username")
    void specialCharactersInUsername() {
        // Act
        user.setUsername("user_name-123");

        assertEquals("user_name-123", user.getUsername());
    }

    @Test
    @DisplayName("Číselný username")
    void numericUsername() {
        // Act
        user.setUsername("12345");

        assertEquals("12345", user.getUsername());
    }

    @Test
    @DisplayName("Email so subdoménou")
    void emailWithSubdomain() {
        // Act
        user.setEmail("user@mail.example.com");

        assertEquals("user@mail.example.com", user.getEmail());
    }

    @Test
    @DisplayName("Email s plus znakom")
    void emailWithPlus() {
        // Act
        user.setEmail("user+tag@example.com");

        assertEquals("user+tag@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Prepísanie existujúcich hodnôt")
    void overwriteExistingValues() {
        // Arrange
        user.setId(1);
        user.setUsername("oldusername");
        user.setEmail("old@example.com");

        // Act
        user.setId(2);
        user.setUsername("newusername");
        user.setEmail("new@example.com");

        assertEquals(2, user.getId());
        assertEquals("newusername", user.getUsername());
        assertEquals("new@example.com", user.getEmail());
    }
}
