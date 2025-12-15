package com.calendar.repository;

import com.calendar.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository rozhranie pre správu udalostí (Event) v databáze.
 *
 * <p>Rozširuje JpaRepository a poskytuje metódy pre CRUD operácie
 * a vlastné dotazy na vyhľadávanie udalostí.</p>
 *
 * <p>Podporuje:
 * <ul>
 *   <li>Vyhľadávanie vlastných udalostí používateľa</li>
 *   <li>Vyhľadávanie zdieľaných udalostí</li>
 *   <li>Filtrovanie udalostí podľa dátumového rozsahu</li>
 *   <li>Kombinované dotazy pre vlastné a zdieľané udalosti</li>
 * </ul></p>
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    /**
     * Vyhľadá všetky udalosti používateľa v zadanom časovom rozsahu.
     *
     * @param userId ID používateľa
     * @param start začiatok časového rozsahu
     * @param end koniec časového rozsahu
     * @return zoznam udalostí používateľa v danom rozsahu
     */
    List<Event> findByUserIdAndStartTimeBetween(Integer userId, LocalDateTime start, LocalDateTime end);

    /**
     * Vyhľadá všetky udalosti patriace konkrétnemu používateľovi.
     *
     * @param userId ID používateľa
     * @return zoznam všetkých udalostí používateľa
     */
    List<Event> findByUserId(Integer userId);

    /**
     * Vyhľadá všetky udalosti zdieľané s konkrétnym používateľom.
     * Používa JOIN na priamo získanie udalostí, kde používateľ je v zozname sharedWith.
     *
     * @param userId ID používateľa, s ktorým sú udalosti zdieľané
     * @return zoznam zdieľaných udalostí
     */
    @Query("SELECT e FROM Event e JOIN e.sharedWith u WHERE u.id = :userId")
    List<Event> findSharedWithUser(@Param("userId") Integer userId);

    /**
     * Vyhľadá zdieľané udalosti v zadanom časovom rozsahu.
     *
     * @param userId ID používateľa, s ktorým sú udalosti zdieľané
     * @param start začiatok časového rozsahu
     * @param end koniec časového rozsahu
     * @return zoznam zdieľaných udalostí v danom rozsahu
     */
    @Query("SELECT e FROM Event e JOIN e.sharedWith u WHERE u.id = :userId AND e.startTime BETWEEN :start AND :end")
    List<Event> findSharedWithUserBetweenDates(
            @Param("userId") Integer userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Vyhľadá všetky udalosti používateľa - vlastné aj zdieľané.
     * Vracia udalosti, kde je používateľ buď vlastníkom alebo má k nim zdieľaný prístup.
     *
     * @param userId ID používateľa
     * @return zoznam všetkých dostupných udalostí (vlastných + zdieľaných)
     */
    @Query("SELECT e FROM Event e WHERE e.user.id = :userId OR :userId IN (SELECT u.id FROM e.sharedWith u)")
    List<Event> findByUserIdOrSharedWithUser(@Param("userId") Integer userId);

    /**
     * Vyhľadá všetky dostupné udalosti používateľa v zadanom časovom rozsahu.
     * Kombinuje vlastné udalosti a zdieľané udalosti.
     *
     * @param userId ID používateľa
     * @param start začiatok časového rozsahu
     * @param end koniec časového rozsahu
     * @return zoznam dostupných udalostí v danom rozsahu
     */
    @Query("SELECT e FROM Event e WHERE (e.user.id = :userId OR :userId IN (SELECT u.id FROM e.sharedWith u)) AND e.startTime BETWEEN :start AND :end")
    List<Event> findByUserIdOrSharedWithUserBetweenDates(
            @Param("userId") Integer userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}