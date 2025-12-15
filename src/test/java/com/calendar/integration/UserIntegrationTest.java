package com.calendar.integration;

import com.calendar.model.User;
import com.calendar.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integračné testy pre User API.
 *
 * <p>Testuje celý flow správy používateľov od HTTP requestu
 * až po databázu vrátane registrácie, prihlásenia a vyhľadávania.</p>
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User API Integration Tests")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Celý registračný flow používateľa")
    void fullUserRegistrationFlow() throws Exception {
        // 1. REGISTER - Registrácia nového používateľa
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@test.com");

        String response = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User registeredUser = objectMapper.readValue(response, User.class);

        // 2. VERIFY - Overíme, že používateľ je v databáze
        User dbUser = userRepository.findById(registeredUser.getId()).orElse(null);
        assert dbUser != null;
        assert dbUser.getUsername().equals("newuser");
        assert dbUser.getEmail().equals("newuser@test.com");

        // 3. FIND BY EMAIL - Vyhľadanie podľa emailu
        mockMvc.perform(get("/api/users/email/newuser@test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@test.com"));
    }

    @Test
    @DisplayName("Login flow - existujúci používateľ")
    void loginExistingUser() throws Exception {
        // Vytvoríme používateľa priamo v DB
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@test.com");
        existingUser = userRepository.save(existingUser);

        // Pokus o prihlásenie
        User loginRequest = new User();
        loginRequest.setUsername("existinguser");
        loginRequest.setEmail("existing@test.com");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.username").value("existinguser"))
                .andExpect(jsonPath("$.email").value("existing@test.com"));
    }

    @Test
    @DisplayName("Login flow - nový používateľ (auto-register)")
    void loginNewUser() throws Exception {
        // Pokus o prihlásenie neexistujúceho používateľa
        User loginRequest = new User();
        loginRequest.setUsername("brandnewuser");
        loginRequest.setEmail("brandnew@test.com");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("brandnewuser"))
                .andExpect(jsonPath("$.email").value("brandnew@test.com"))
                .andExpect(jsonPath("$.id").exists());

        // Overíme, že používateľ bol vytvorený v DB
        User dbUser = userRepository.findByUsername("brandnewuser");
        assert dbUser != null;
        assert dbUser.getEmail().equals("brandnew@test.com");
    }

    @Test
    @DisplayName("Find or create - existujúci používateľ")
    void findOrCreateExistingUser() throws Exception {
        // Vytvoríme používateľa
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user = userRepository.save(user);

        // Find or create pre existujúceho
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");

        mockMvc.perform(post("/api/users/find-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Find or create - nový používateľ")
    void findOrCreateNewUser() throws Exception {
        // Find or create pre neexistujúceho
        Map<String, String> request = new HashMap<>();
        request.put("email", "newperson@example.com");

        mockMvc.perform(post("/api/users/find-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newperson"))
                .andExpect(jsonPath("$.email").value("newperson@example.com"))
                .andExpect(jsonPath("$.id").exists());

        // Overíme v databáze
        User dbUser = userRepository.findByEmail("newperson@example.com");
        assert dbUser != null;
        assert dbUser.getUsername().equals("newperson");
    }

    @Test
    @DisplayName("Vyhľadávanie používateľov")
    void searchUsers() throws Exception {
        // Vytvoríme 3 používateľov
        userRepository.save(new User(null, "alice", "alice@test.com"));
        userRepository.save(new User(null, "bob", "bob@test.com"));
        userRepository.save(new User(null, "charlie", "charlie@test.com"));

        // Vyhľadávanie podľa "ali"
        mockMvc.perform(get("/api/users/search")
                        .param("term", "ali")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("alice"));

        // Vyhľadávanie podľa "@test.com"
        mockMvc.perform(get("/api/users/search")
                        .param("term", "@test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("Získanie všetkých používateľov")
    void getAllUsers() throws Exception {
        // Vytvoríme 3 používateľov
        userRepository.save(new User(null, "user1", "user1@test.com"));
        userRepository.save(new User(null, "user2", "user2@test.com"));
        userRepository.save(new User(null, "user3", "user3@test.com"));

        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[1].username").exists())
                .andExpect(jsonPath("$[2].username").exists());
    }

    @Test
    @DisplayName("Admin - aktualizácia používateľa")
    void updateUser() throws Exception {
        // Vytvoríme používateľa
        User user = new User();
        user.setUsername("oldname");
        user.setEmail("old@test.com");
        user = userRepository.save(user);

        // Aktualizujeme údaje
        User updateData = new User();
        updateData.setUsername("newname");
        updateData.setEmail("new@test.com");

        mockMvc.perform(put("/api/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newname"))
                .andExpect(jsonPath("$.email").value("new@test.com"));

        // Overíme v databáze
        User dbUser = userRepository.findById(user.getId()).orElse(null);
        assert dbUser != null;
        assert dbUser.getUsername().equals("newname");
        assert dbUser.getEmail().equals("new@test.com");
    }

    @Test
    @DisplayName("Admin - vymazanie používateľa")
    void deleteUser() throws Exception {
        // Vytvoríme používateľa
        User user = new User();
        user.setUsername("todelete");
        user.setEmail("delete@test.com");
        user = userRepository.save(user);
        Integer userId = user.getId();

        // Vymažeme používateľa
        mockMvc.perform(delete("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        // Overíme, že je vymazaný
        User dbUser = userRepository.findById(userId).orElse(null);
        assert dbUser == null;
    }

    @Test
    @DisplayName("Chyba - vyhľadanie neexistujúceho používateľa")
    void findNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/users/email/nonexistent@test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("Chyba - find-or-create bez emailu")
    void findOrCreateWithoutEmail() throws Exception {
        Map<String, String> request = new HashMap<>();
        // email chýba!

        mockMvc.perform(post("/api/users/find-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is required"));
    }

    @Test
    @DisplayName("Chyba - aktualizácia neexistujúceho používateľa")
    void updateNonExistentUser() throws Exception {
        User updateData = new User();
        updateData.setUsername("newname");
        updateData.setEmail("new@test.com");

        mockMvc.perform(put("/api/users/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Chyba - vymazanie neexistujúceho používateľa")
    void deleteNonExistentUser() throws Exception {
        mockMvc.perform(delete("/api/users/999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Persistencia a konzistencia dát")
    void dataPersistenceAndConsistency() throws Exception {
        // Vytvoríme používateľa cez API
        User user = new User();
        user.setUsername("persistencetest");
        user.setEmail("persistence@test.com");

        String response = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(response, User.class);

        // Overíme v databáze
        User dbUser1 = userRepository.findById(createdUser.getId()).orElse(null);
        User dbUser2 = userRepository.findByUsername("persistencetest");
        User dbUser3 = userRepository.findByEmail("persistence@test.com");

        // Všetky tri spôsoby vyhľadania by mali vrátiť toho istého používateľa
        assert dbUser1 != null && dbUser2 != null && dbUser3 != null;
        assert dbUser1.getId().equals(dbUser2.getId());
        assert dbUser2.getId().equals(dbUser3.getId());
        assert dbUser1.getUsername().equals("persistencetest");
        assert dbUser1.getEmail().equals("persistence@test.com");
    }
}