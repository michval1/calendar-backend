package com.calendar.repository;

import com.calendar.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByUserIdAndStartTimeBetween(Integer userId, LocalDateTime start, LocalDateTime end);
    List<Event> findByUserId(Integer userId);

    // New methods for shared events

    // Get events shared with a specific user
    @Query("SELECT e FROM Event e JOIN e.sharedWith u WHERE u.id = :userId")
    List<Event> findSharedWithUser(@Param("userId") Integer userId);

    // Get events shared with a specific user within a date range
    @Query("SELECT e FROM Event e JOIN e.sharedWith u WHERE u.id = :userId AND e.startTime BETWEEN :start AND :end")
    List<Event> findSharedWithUserBetweenDates(
            @Param("userId") Integer userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Get both user's events and events shared with them
    @Query("SELECT e FROM Event e WHERE e.user.id = :userId OR :userId IN (SELECT u.id FROM e.sharedWith u)")
    List<Event> findByUserIdOrSharedWithUser(@Param("userId") Integer userId);

    // Get both user's events and events shared with them within a date range
    @Query("SELECT e FROM Event e WHERE (e.user.id = :userId OR :userId IN (SELECT u.id FROM e.sharedWith u)) AND e.startTime BETWEEN :start AND :end")
    List<Event> findByUserIdOrSharedWithUserBetweenDates(
            @Param("userId") Integer userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}