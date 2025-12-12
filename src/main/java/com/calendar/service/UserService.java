package com.calendar.service;

import com.calendar.model.User;
import com.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            return userRepository.save(user);
        }

        return user;
    }

    public void saveActiveUser(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("Aktívny používateľ: " + user.getUsername());

        } else {
            throw new RuntimeException("Používateľ s ID " + userId + " neexistuje");
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findOrCreateUserByEmail(String email) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            String username = email.split("@")[0];

            String baseUsername = username;
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter++;
            }

            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            return userRepository.save(user);
        }

        return user;
    }

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
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}