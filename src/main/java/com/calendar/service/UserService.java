package com.calendar.service;

import com.calendar.model.User;
import com.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servisná vrstva pre správu používateľov.
 *
 * <p>UserService poskytuje business logiku pre registráciu, autentifikáciu
 * a správu používateľských účtov v aplikácii.</p>
 *
 * <p>Hlavné funkcie:
 * <ul>
 *   <li>Registrácia nových používateľov</li>
 *   <li>Prihlásenie existujúcich používateľov</li>
 *   <li>Vyhľadávanie používateľov</li>
 *   <li>Aktualizácia a mazanie používateľov (admin funkcie)</li>
 * </ul></p>
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Zaregistruje nového používateľa v systéme.
     *
     * @param user nový používateľ na registráciu
     * @return uložený používateľ s vygenerovaným ID
     */
    public User registerUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Prihlási používateľa do systému.
     * Ak používateľ neexistuje, automaticky ho vytvorí s danými údajmi.
     * Používa sa pri autentifikácii cez Firebase.
     *
     * @param username prihlasovacie meno používateľa
     * @param email emailová adresa používateľa
     * @return existujúci alebo novo vytvorený používateľ
     */
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

    /**
     * Uloží aktívneho používateľa (helper metóda pre debugging).
     * Vypíše informáciu o aktívnom používateľovi do konzoly.
     *
     * @param userId ID používateľa
     * @throws RuntimeException ak používateľ neexistuje
     */
    public void saveActiveUser(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("Aktívny používateľ: " + user.getUsername());

        } else {
            throw new RuntimeException("Používateľ s ID " + userId + " neexistuje");
        }
    }

    /**
     * Vyhľadá používateľa podľa emailovej adresy.
     *
     * @param email emailová adresa používateľa
     * @return používateľ s daným emailom alebo null
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Nájde alebo vytvorí používateľa podľa emailu.
     * Ak používateľ s daným emailom neexistuje, vytvorí nového používateľa
     * s automaticky vygenerovaným unikátnym prihlasovacím menom.
     * Používa sa pri zdieľaní udalostí s novými používateľmi.
     *
     * @param email emailová adresa používateľa
     * @return existujúci alebo novo vytvorený používateľ
     */
    public User findOrCreateUserByEmail(String email) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            // Vytvor username z emailu (časť pred @)
            String username = email.split("@")[0];

            // Zabezpeč unikátnosť username pridaním čísla
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
     * Vymaže používateľa podľa ID.
     * Používa sa v admin paneli.
     *
     * @param userId ID používateľa na vymazanie
     * @throws RuntimeException ak používateľ neexistuje
     */
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    /**
     * Aktualizuje údaje používateľa.
     * Aktualizuje len polia, ktoré sú v userDetails neprázdné.
     * Používa sa v admin paneli.
     *
     * @param userId ID používateľa na aktualizáciu
     * @param userDetails nové údaje používateľa
     * @return aktualizovaný používateľ
     * @throws RuntimeException ak používateľ neexistuje
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
     * Vyhľadá používateľov podľa vyhľadávacieho reťazca.
     * Hľadá v používateľskom mene aj emaile (case-insensitive).
     * Ak je searchTerm prázdny, vráti všetkých používateľov.
     * Používa sa v admin paneli a pri zdieľaní udalostí.
     *
     * @param searchTerm vyhľadávací reťazec
     * @return zoznam používateľov zodpovedajúcich vyhľadávaniu
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

    /**
     * Vráti zoznam všetkých používateľov v systéme.
     * Používa sa v admin paneli.
     *
     * @return zoznam všetkých používateľov
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}