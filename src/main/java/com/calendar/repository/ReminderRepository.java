package com.calendar.repository;

import com.calendar.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository rozhranie pre správu pripomienok (Reminder) v databáze.
 *
 * <p>Poskytuje metódy na vyhľadávanie, vytváranie a mazanie pripomienok
 * spojených s udalosťami a používateľmi.</p>
 *
 * <p>Podporuje:
 * <ul>
 *   <li>Vyhľadávanie pripomienok podľa používateľa</li>
 *   <li>Vyhľadávanie pripomienok podľa udalosti</li>
 *   <li>Vyhľadávanie čakajúcich (pending) pripomienok</li>
 *   <li>Vyhľadávanie nadchádzajúcich pripomienok</li>
 * </ul></p>
 */
@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Integer> {

    /**
     * Vyhľadá všetky pripomienky konkrétneho používateľa.
     *
     * @param userId ID používateľa
     * @return zoznam všetkých pripomienok používateľa
     */
    List<Reminder> findByUserId(Integer userId);

    /**
     * Vyhľadá všetky pripomienky pre konkrétnu udalosť.
     *
     * @param eventId ID udalosti
     * @return zoznam všetkých pripomienok danej udalosti
     */
    List<Reminder> findByEventId(Integer eventId);

    /**
     * Vyhľadá čakajúce pripomienky používateľa, ktoré ešte neboli odoslané
     * a ich čas už nastal alebo prešiel.
     * Používa sa na spracovanie pripomienok, ktoré je potrebné odoslať.
     *
     * @param userId ID používateľa
     * @param currentTime aktuálny čas
     * @return zoznam pripomienok na odoslanie
     */
    @Query("SELECT r FROM Reminder r WHERE r.user.id = :userId AND r.isSent = false AND r.reminderTime <= :currentTime")
    List<Reminder> findPendingRemindersForUser(@Param("userId") Integer userId, @Param("currentTime") LocalDateTime currentTime);

    /**
     * Vyhľadá nadchádzajúce pripomienky používateľa v zadanom časovom okne.
     * Vracia len neodoslané pripomienky, zoradené podľa času vzostupne.
     * Používa sa na zobrazenie pripravujúcich sa pripomienok.
     *
     * @param userId ID používateľa
     * @param now aktuálny čas
     * @param futureTime koniec časového okna
     * @return zoradený zoznam nadchádzajúcich pripomienok
     */
    @Query("SELECT r FROM Reminder r WHERE r.user.id = :userId AND r.isSent = false AND r.reminderTime BETWEEN :now AND :futureTime ORDER BY r.reminderTime ASC")
    List<Reminder> findUpcomingRemindersForUser(
            @Param("userId") Integer userId,
            @Param("now") LocalDateTime now,
            @Param("futureTime") LocalDateTime futureTime
    );

    /**
     * Vymaže všetky pripomienky pre konkrétnu udalosť.
     * Používa sa pri vymazaní udalosti alebo aktualizácii jej pripomienok.
     *
     * @param eventId ID udalosti
     */
    void deleteByEventId(Integer eventId);

    /**
     * Vyhľadá pripomienky pre konkrétnu kombináciu udalosti a používateľa.
     * Používa sa pri aktualizácii pripomienok zdieľaných udalostí.
     *
     * @param eventId ID udalosti
     * @param userId ID používateľa
     * @return zoznam pripomienok pre danú udalosť a používateľa
     */
    @Query("SELECT r FROM Reminder r WHERE r.event.id = :eventId AND r.user.id = :userId")
    List<Reminder> findByEventIdAndUserId(@Param("eventId") Integer eventId, @Param("userId") Integer userId);
}