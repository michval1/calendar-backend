package com.calendar.controller;

import com.calendar.model.User;
import com.calendar.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Unit testy pre UserController.
 * 
 * <p>Testuje REST API endpointy pre správu používateľov.</p>
 * 
 * @author Andrej
 * @version 1.0
 * @since 2024
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    @DisplayName("POST /api/users/register - registrácia používateľa")
    void registerUser_Success() throws Exception {
        // Arrange
        when(userService.registerUser(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    @DisplayName("POST /api/users/register - chyba pri registrácii")
    void registerUser_Failure() throws Exception {
        // Arrange
        when(userService.registerUser(any(User.class)))
            .thenThrow(new RuntimeException("Email already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Registration failed")));
    }

    @Test
    @DisplayName("POST /api/users/login - prihlásenie používateľa")
    void loginUser_Success() throws Exception {
        // Arrange
        when(userService.loginUser("testuser", "test@example.com")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")));

        verify(userService, times(1)).loginUser("testuser", "test@example.com");
    }

    @Test
    @DisplayName("POST /api/users/active - uloženie aktívneho používateľa")
    void saveActiveUser_Success() throws Exception {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("userId", "1");
        
        doNothing().when(userService).saveActiveUser(1);

        // Act & Assert
        mockMvc.perform(post("/api/users/active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Active user saved successfully")));

        verify(userService, times(1)).saveActiveUser(1);
    }

    @Test
    @DisplayName("POST /api/users/active - chýbajúce userId")
    void saveActiveUser_MissingUserId() throws Exception {
        // Arrange
        Map<String, String> payload = new HashMap<>();

        // Act & Assert
        mockMvc.perform(post("/api/users/active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("User ID is required")));

        verify(userService, never()).saveActiveUser(anyInt());
    }

    @Test
    @DisplayName("POST /api/users/active - neplatný formát userId")
    void saveActiveUser_InvalidUserId() throws Exception {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("userId", "invalid");

        // Act & Assert
        mockMvc.perform(post("/api/users/active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid user ID format")));
    }

    @Test
    @DisplayName("GET /api/users/email/{email} - nájdenie používateľa")
    void getUserByEmail_Success() throws Exception {
        // Arrange
        when(userService.findByEmail("test@example.com")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/email/test@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")));

        verify(userService, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("GET /api/users/email/{email} - používateľ nenájdený")
    void getUserByEmail_NotFound() throws Exception {
        // Arrange
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/users/email/nonexistent@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("POST /api/users/find-or-create - nájdenie alebo vytvorenie")
    void findOrCreateUser_Success() throws Exception {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "test@example.com");
        
        when(userService.findOrCreateUserByEmail("test@example.com")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/users/find-or-create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")));

        verify(userService, times(1)).findOrCreateUserByEmail("test@example.com");
    }

    @Test
    @DisplayName("POST /api/users/find-or-create - chýbajúci email")
    void findOrCreateUser_MissingEmail() throws Exception {
        // Arrange
        Map<String, String> payload = new HashMap<>();

        // Act & Assert
        mockMvc.perform(post("/api/users/find-or-create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Email is required")));
    }

    @Test
    @DisplayName("GET /api/users - získanie všetkých používateľov (admin)")
    void getAllUsers_Success() throws Exception {
        // Arrange
        User user2 = new User();
        user2.setId(2);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        
        List<User> users = Arrays.asList(testUser, user2);
        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("testuser")))
                .andExpect(jsonPath("$[1].username", is("user2")));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("DELETE /api/users/{userId} - vymazanie používateľa (admin)")
    void deleteUser_Success() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User deleted successfully")));

        verify(userService, times(1)).deleteUser(1);
    }

    @Test
    @DisplayName("DELETE /api/users/{userId} - chyba pri vymazávaní")
    void deleteUser_Failure() throws Exception {
        // Arrange
        doThrow(new RuntimeException("User not found")).when(userService).deleteUser(999);

        // Act & Assert
        mockMvc.perform(delete("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("Failed to delete user")));
    }

    @Test
    @DisplayName("PUT /api/users/{userId} - aktualizácia používateľa (admin)")
    void updateUser_Success() throws Exception {
        // Arrange
        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        
        when(userService.updateUser(eq(1), any(User.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updateduser")))
                .andExpect(jsonPath("$.email", is("updated@example.com")));

        verify(userService, times(1)).updateUser(eq(1), any(User.class));
    }

    @Test
    @DisplayName("PUT /api/users/{userId} - chyba pri aktualizácii")
    void updateUser_Failure() throws Exception {
        // Arrange
        when(userService.updateUser(eq(999), any(User.class)))
            .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(put("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Failed to update user")));
    }

    @Test
    @DisplayName("GET /api/users/search - vyhľadávanie používateľov")
    void searchUsers_Success() throws Exception {
        // Arrange
        List<User> searchResults = Arrays.asList(testUser);
        when(userService.searchUsers("test")).thenReturn(searchResults);

        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .param("term", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("testuser")));

        verify(userService, times(1)).searchUsers("test");
    }

    @Test
    @DisplayName("GET /api/users/search - prázdny výsledok")
    void searchUsers_NoResults() throws Exception {
        // Arrange
        when(userService.searchUsers("nonexistent")).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .param("term", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/users - chyba pri získavaní používateľov")
    void getAllUsers_ServiceError() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("Failed to fetch users")));
    }
}
