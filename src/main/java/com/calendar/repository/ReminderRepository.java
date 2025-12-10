package com.calendar.repository;

import com.calendar.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Integer> {

    // Find all reminders for a specific user
    List<Reminder> findByUserId(Integer userId);

    // Find all reminders for a specific event
    List<Reminder> findByEventId(Integer eventId);

    // Find pending reminders for a user (not sent yet, and time has come)
    @Query("SELECT r FROM Reminder r WHERE r.user.id = :userId AND r.isSent = false AND r.reminderTime <= :currentTime")
    List<Reminder> findPendingRemindersForUser(@Param("userId") Integer userId, @Param("currentTime") LocalDateTime currentTime);

    // Find upcoming reminders for a user (not sent yet, within next X minutes)
    @Query("SELECT r FROM Reminder r WHERE r.user.id = :userId AND r.isSent = false AND r.reminderTime BETWEEN :now AND :futureTime ORDER BY r.reminderTime ASC")
    List<Reminder> findUpcomingRemindersForUser(
            @Param("userId") Integer userId,
            @Param("now") LocalDateTime now,
            @Param("futureTime") LocalDateTime futureTime
    );

    // Delete all reminders for a specific event
    void deleteByEventId(Integer eventId);

    // Find reminders by event and user
    @Query("SELECT r FROM Reminder r WHERE r.event.id = :eventId AND r.user.id = :userId")
    List<Reminder> findByEventIdAndUserId(@Param("eventId") Integer eventId, @Param("userId") Integer userId);
}