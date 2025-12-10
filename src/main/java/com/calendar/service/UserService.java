package com.calendar.service;

import com.calendar.model.User;
import com.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user) {
        return userRepository.save(user);
    }

    public User loginUser(String username, String email) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            // User doesn't exist, create new user
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            return userRepository.save(user);
        }

        return user;
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public void saveActiveUser(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Tu môžete pridať akýkoľvek kód pre spracovanie aktívneho používateľa
            // Napríklad aktualizácia stavu, timestamp a pod.
            System.out.println("Aktívny používateľ: " + user.getUsername());

            // Príklad: aktualizovať používateľa ak je potrebné
            // userRepository.save(user);
        } else {
            throw new RuntimeException("Používateľ s ID " + userId + " neexistuje");
        }
    }

    // New methods for finding users by email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Get all users (for sharing events)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Find or create a user by email
    public User findOrCreateUserByEmail(String email) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            // Generate a username based on email
            String username = email.split("@")[0];

            // Check if username exists, append numbers if needed
            String baseUsername = username;
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter++;
            }

            // Create new user
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            return userRepository.save(user);
        }

        return user;
    }

    // ADMIN OPERATIONS

    /**
     * Delete a user by ID
     */
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    /**
     * Update a user
     */
    public User updateUser(Integer userId, User userDetails) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        User existingUser = userOptional.get();

        // Update fields
        if (userDetails.getUsername() != null) {
            existingUser.setUsername(userDetails.getUsername());
        }
        if (userDetails.getEmail() != null) {
            existingUser.setEmail(userDetails.getEmail());
        }

        return userRepository.save(existingUser);
    }

    /**
     * Search users by username or email
     */
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }

        List<User> allUsers = userRepository.findAll();
        String lowerSearchTerm = searchTerm.toLowerCase();

        return allUsers.stream()
                .filter(user ->
                        user.getUsername().toLowerCase().contains(lowerSearchTerm) ||
                                user.getEmail().toLowerCase().contains(lowerSearchTerm)
                )
                .collect(Collectors.toList());
    }
}