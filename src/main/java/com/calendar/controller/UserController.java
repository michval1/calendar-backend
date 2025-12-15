package com.calendar.controller;

import com.calendar.model.User;
import com.calendar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller pre správu používateľov.
 *
 * <p>Poskytuje RESTful API endpointy pre:
 * <ul>
 *   <li>Registráciu a prihlásenie používateľov</li>
 *   <li>Vyhľadávanie používateľov</li>
 *   <li>Admin operácie (CRUD používateľov)</li>
 * </ul></p>
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.10:3000"})
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Zaregistruje nového používateľa v systéme.
     *
     * <p>Endpoint: POST /api/users/register</p>
     *
     * @param user údaje nového používateľa (JSON v request body)
     * @return ResponseEntity s registrovaným používateľom alebo chybovou správou
     *
     * @example POST /api/users/register
     *          Body: {"username": "john_doe", "email": "john@example.com"}
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Registration failed: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Prihlási používateľa do systému.
     *
     * <p>Endpoint: POST /api/users/login</p>
     *
     * <p>Ak používateľ neexistuje, automaticky ho vytvorí.
     * Používa sa po Firebase autentifikácii.</p>
     *
     * @param user objekt s username a email (JSON v request body)
     * @return ResponseEntity s prihláseným používateľom alebo chybovou správou
     *
     * @example POST /api/users/login
     *          Body: {"username": "john_doe", "email": "john@example.com"}
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        try {
            User loggedInUser = userService.loginUser(user.getUsername(), user.getEmail());
            return new ResponseEntity<>(loggedInUser, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Login failed: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Uloží aktívneho používateľa (helper endpoint pre debugging).
     *
     * <p>Endpoint: POST /api/users/active</p>
     *
     * @param payload objekt s userId (JSON v request body)
     * @return ResponseEntity s potvrdením alebo chybovou správou
     *
     * @example POST /api/users/active
     *          Body: {"userId": "1"}
     */
    @PostMapping("/active")
    public ResponseEntity<?> saveActiveUser(@RequestBody Map<String, String> payload) {
        try {
            String userId = payload.get("userId");
            if (userId == null || userId.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "User ID is required");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            Integer userIdInt = Integer.parseInt(userId);
            userService.saveActiveUser(userIdInt);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Active user saved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NumberFormatException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid user ID format");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to save active user: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Vyhľadá používateľa podľa emailovej adresy.
     *
     * <p>Endpoint: GET /api/users/email/{email}</p>
     *
     * @param email emailová adresa používateľa (path parameter)
     * @return ResponseEntity s používateľom alebo chybovou správou
     *
     * @example GET /api/users/email/john@example.com
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.findByEmail(email);
            if (user == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "User with email " + email + " not found");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch user: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Nájde alebo vytvorí používateľa podľa emailu.
     *
     * <p>Endpoint: POST /api/users/find-or-create</p>
     *
     * <p>Ak používateľ s emailom neexistuje, vytvorí nového.
     * Používa sa pri zdieľaní udalostí s novými používateľmi.</p>
     *
     * @param payload objekt s email (JSON v request body)
     * @return ResponseEntity s nájdeným/vytvoreným používateľom
     *
     * @example POST /api/users/find-or-create
     *          Body: {"email": "new_user@example.com"}
     */
    @PostMapping("/find-or-create")
    public ResponseEntity<?> findOrCreateUserByEmail(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            if (email == null || email.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Email is required");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            User user = userService.findOrCreateUserByEmail(email);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to find or create user: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * ADMIN ENDPOINT - Vráti všetkých používateľov v systéme.
     *
     * <p>Endpoint: GET /api/users</p>
     *
     * @return ResponseEntity so zoznamom všetkých používateľov
     *
     * @apiNote Tento endpoint by mal byť chránený autorizáciou na admin rolu.
     *
     * @example GET /api/users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch users: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ADMIN ENDPOINT - Vymaže používateľa.
     *
     * <p>Endpoint: DELETE /api/users/{userId}</p>
     *
     * @param userId ID používateľa na vymazanie (path parameter)
     * @return ResponseEntity s potvrdením alebo chybovou správou
     *
     * @apiNote Tento endpoint by mal byť chránený autorizáciou na admin rolu.
     *
     * @example DELETE /api/users/123
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId) {
        try {
            userService.deleteUser(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to delete user: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ADMIN ENDPOINT - Aktualizuje údaje používateľa.
     *
     * <p>Endpoint: PUT /api/users/{userId}</p>
     *
     * @param userId ID používateľa na aktualizáciu (path parameter)
     * @param userDetails nové údaje používateľa (JSON v request body)
     * @return ResponseEntity s aktualizovaným používateľom
     *
     * @apiNote Tento endpoint by mal byť chránený autorizáciou na admin rolu.
     *
     * @example PUT /api/users/123
     *          Body: {"username": "new_username", "email": "new@example.com"}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Integer userId, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(userId, userDetails);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to update user: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Vyhľadá používateľov podľa vyhľadávacieho reťazca.
     *
     * <p>Endpoint: GET /api/users/search?term={searchTerm}</p>
     *
     * <p>Hľadá v používateľskom mene aj emaile (case-insensitive).
     * Používa sa pri zdieľaní udalostí na nájdenie používateľov.</p>
     *
     * @param term vyhľadávací reťazec (query parameter)
     * @return ResponseEntity so zoznamom nájdených používateľov
     *
     * @example GET /api/users/search?term=john
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String term) {
        try {
            List<User> users = userService.searchUsers(term);
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to search users: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}