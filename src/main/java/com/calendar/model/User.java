package com.calendar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entita reprezentujúca používateľa kalendár aplikácie.
 *
 * <p>Používateľ je centrálna entita systému, ktorá vlastní udalosti,
 * kategórie a upomienky. Každý používateľ má unikátne prihlasovacie meno
 * a emailovú adresu.</p>
 *
 * <p>Autentifikácia používateľov je zabezpečená cez Firebase Authentication,
 * pričom táto entita slúži na uchovanie základných informácií o používateľovi
 * v lokálnej databáze.</p>
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {

    /**
     * Unikátny identifikátor používateľa (primárny kľúč).
     * Automaticky generovaný pri registrácii nového používateľa.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer id;

    /**
     * Prihlasovacie meno používateľa.
     * Musí byť povinné.
     * Dĺžka: 1-50 znakov.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 1, max = 50, message = "Username must be between 1 and 50 characters")
    @Column(name = "Username", unique = true, nullable = false)
    private String username;

    /**
     * Emailová adresa používateľa.
     * Musí byť platná emailová adresa, unikátna a povinná.
     * Používa sa pri autentifikácii cez Firebase.
     * Maximálna dĺžka: 100 znakov.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "Email", unique = true, nullable = false)
    private String email;

    /**
     * Získa ID používateľa.
     * @return unikátny identifikátor používateľa
     */
    public Integer getId() {
        return id;
    }

    /**
     * Získa email používateľa.
     * @return emailová adresa používateľa
     */
    public String getEmail() {
        return email;
    }

    /**
     * Získa používateľské meno.
     * @return prihlasovacie meno používateľa
     */
    public String getUsername() {
        return username;
    }

    /**
     * Nastaví email používateľa.
     * @param email nová emailová adresa
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Nastaví ID používateľa.
     * @param id nový identifikátor
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Nastaví používateľské meno.
     * @param username nové prihlasovacie meno
     */
    public void setUsername(String username) {
        this.username = username;
    }
}