package com.calendar.repository;

import com.calendar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository rozhranie pre správu používateľov (User) v databáze.
 *
 * <p>Poskytuje metódy na vyhľadávanie používateľov podľa rôznych kritérií
 * a kontrolu existencie používateľov v systéme.</p>
 *
 * <p>Používa sa pri autentifikácii, registrácii a zdieľaní udalostí
 * medzi používateľmi.</p>
 *
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Vyhľadá používateľa podľa prihlasovacieho mena.
     *
     * @param username prihlasovacie meno používateľa
     * @return používateľ s daným menom alebo null ak neexistuje
     */
    User findByUsername(String username);

    /**
     * Vyhľadá používateľa podľa emailovej adresy.
     *
     * @param email emailová adresa používateľa
     * @return používateľ s daným emailom alebo null ak neexistuje
     */
    User findByEmail(String email);

    /**
     * Kontroluje, či existuje používateľ s daným prihlasovacím menom.
     * Používa sa pri registrácii na kontrolu unikátnosti.
     *
     * @param username prihlasovacie meno na kontrolu
     * @return true ak používateľ s týmto menom existuje
     */
    boolean existsByUsername(String username);

    /**
     * Kontroluje, či existuje používateľ s danou emailovou adresou.
     * Používa sa pri registrácii na kontrolu unikátnosti.
     *
     * @param email emailová adresa na kontrolu
     * @return true ak používateľ s týmto emailom existuje
     */
    boolean existsByEmail(String email);
}