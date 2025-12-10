package com.calendar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EventID")
    private Integer id;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Description")
    private String description;

    @Column(name = "Location")
    private String location;

    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "IsAllDay")
    private Boolean isAllDay = false;

    @Column(name = "RecurrenceType")
    private String recurrenceType;

    @Column(name = "RecurrenceEnd")
    private LocalDateTime recurrenceEnd;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    @Column(name = "Priority")
    private String priority = "MEDIUM";

    @Column(name = "Color")
    private String color;

    @Column(name = "IsShared")
    private Boolean isShared = false;

    @ManyToMany
    @JoinTable(
            name = "event_shared_users",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "userPermissions_KEY")
    )
    private Set<User> sharedWith = new HashSet<>();

    // Transient field to receive permissions from frontend
    // This is not persisted to database, but used to transfer permission data
    @Transient
    private Map<Integer, String> userPermissions = new HashMap<>();

    // NEW: Transient field for reminder minutes (e.g., [15, 30, 60])
    // This is sent/received from frontend but not stored directly in event table
    // Actual reminders are stored in the reminder table
    @Transient
    private List<Integer> reminderMinutes = new ArrayList<>();

    // Manually defined getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Boolean getIsAllDay() {
        return isAllDay;
    }

    public void setIsAllDay(Boolean allDay) {
        isAllDay = allDay;
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(String recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public void setRecurrenceEnd(LocalDateTime recurrenceEnd) {
        this.recurrenceEnd = recurrenceEnd;
    }

    public LocalDateTime getRecurrenceEnd() {
        return recurrenceEnd;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getIsShared() {
        return isShared;
    }

    public void setIsShared(Boolean shared) {
        isShared = shared;
    }

    public Set<User> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(Set<User> sharedWith) {
        this.sharedWith = sharedWith;
    }

    public Map<Integer, String> getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(Map<Integer, String> userPermissions) {
        this.userPermissions = userPermissions;
    }

    public List<Integer> getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(List<Integer> reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

    public void shareWithUser(User user) {
        this.sharedWith.add(user);
        this.isShared = true;
    }

    public void removeSharedUser(User user) {
        this.sharedWith.remove(user);
        if (this.sharedWith.isEmpty()) {
            this.isShared = false;
        }
    }
}