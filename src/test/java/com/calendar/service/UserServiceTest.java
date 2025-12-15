package com.calendar.service;

import com.calendar.model.User;
import com.calendar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit testy pre UserService.
 * 
 * <p>Testuje správanie UserService vrátane registrácie, prihlásenia,
 * vyhľadávania a správy používateľov.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    @DisplayName("Registrácia používateľa - úspešná")
    void registerUser_Success() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User registeredUser = userService.registerUser(testUser);

        assertNotNull(registeredUser);
        assertEquals("testuser", registeredUser.getUsername());
        assertEquals("test@example.com", registeredUser.getEmail());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Prihlásenie používateľa - existujúci používateľ")
    void loginUser_ExistingUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        User loggedInUser = userService.loginUser("testuser", "test@example.com");

        assertNotNull(loggedInUser);
        assertEquals(1, loggedInUser.getId());
        assertEquals("testuser", loggedInUser.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Prihlásenie používateľa - nový používateľ")
    void loginUser_NewUser() {
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        
        User newUser = new User();
        newUser.setId(2);
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User loggedInUser = userService.loginUser("newuser", "newuser@example.com");

        assertNotNull(loggedInUser);
        assertEquals("newuser", loggedInUser.getUsername());
        assertEquals("newuser@example.com", loggedInUser.getEmail());
        verify(userRepository, times(1)).findByUsername("newuser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Vyhľadanie používateľa podľa emailu - úspešné")
    void findByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

        User foundUser = userService.findByEmail("test@example.com");

        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Vyhľadanie používateľa podľa emailu - nenájdený")
    void findByEmail_NotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        User foundUser = userService.findByEmail("nonexistent@example.com");

        assertNull(foundUser);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Nájdenie alebo vytvorenie používateľa - existujúci")
    void findOrCreateUserByEmail_ExistingUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

        User user = userService.findOrCreateUserByEmail("test@example.com");

        assertNotNull(user);
        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Nájdenie alebo vytvorenie používateľa - nový")
    void findOrCreateUserByEmail_NewUser() {
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(null);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        
        User newUser = new User();
        newUser.setId(2);
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User user = userService.findOrCreateUserByEmail("newuser@example.com");

        assertNotNull(user);
        assertEquals("newuser", user.getUsername());
        assertEquals("newuser@example.com", user.getEmail());
        verify(userRepository, times(1)).findByEmail("newuser@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Vytvorenie používateľa s konfliktným username")
    void findOrCreateUserByEmail_UsernameConflict() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(null);
        when(userRepository.existsByUsername("john")).thenReturn(true);
        when(userRepository.existsByUsername("john1")).thenReturn(false);
        
        User newUser = new User();
        newUser.setId(3);
        newUser.setUsername("john1");
        newUser.setEmail("john@example.com");
        
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User user = userService.findOrCreateUserByEmail("john@example.com");

        assertNotNull(user);
        assertEquals("john1", user.getUsername());
        verify(userRepository, times(1)).existsByUsername("john");
        verify(userRepository, times(1)).existsByUsername("john1");
    }

    @Test
    @DisplayName("Vymazanie používateľa - úspešné")
    void deleteUser_Success() {
        when(userRepository.existsById(1)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1);

        userService.deleteUser(1);

        verify(userRepository, times(1)).existsById(1);
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Vymazanie používateľa - neexistuje")
    void deleteUser_NotFound() {
        when(userRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(999);
        });
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("Aktualizácia používateľa - úspešná")
    void updateUser_Success() {
        User updatedDetails = new User();
        updatedDetails.setUsername("updateduser");
        updatedDetails.setEmail("updated@example.com");
        
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updatedUser = userService.updateUser(1, updatedDetails);

        assertNotNull(updatedUser);
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Aktualizácia používateľa - neexistuje")
    void updateUser_NotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(999, testUser);
        });
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Vyhľadávanie používateľov - podľa username")
    void searchUsers_ByUsername() {
        User user2 = new User();
        user2.setId(2);
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        
        List<User> allUsers = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(allUsers);

        List<User> results = userService.searchUsers("testuser");

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Vyhľadávanie používateľov - podľa emailu")
    void searchUsers_ByEmail() {
        List<User> allUsers = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(allUsers);

        List<User> results = userService.searchUsers("test@");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("test@example.com", results.get(0).getEmail());
    }

    @Test
    @DisplayName("Vyhľadávanie používateľov - prázdny výsledok")
    void searchUsers_NoResults() {
        List<User> allUsers = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(allUsers);

        List<User> results = userService.searchUsers("nonexistent");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Vyhľadávanie používateľov - prázdny vyhľadávací reťazec")
    void searchUsers_EmptySearchTerm() {
        List<User> allUsers = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(allUsers);

        List<User> results = userService.searchUsers("");

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Získanie všetkých používateľov")
    void getAllUsers_Success() {
        User user2 = new User();
        user2.setId(2);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        
        List<User> allUsers = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(allUsers);

        List<User> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Uloženie aktívneho používateľa - úspešné")
    void saveActiveUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertDoesNotThrow(() -> userService.saveActiveUser(1));
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Uloženie aktívneho používateľa - neexistuje")
    void saveActiveUser_NotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.saveActiveUser(999);
        });
        
        assertTrue(exception.getMessage().contains("neexistuje"));
    }
}
