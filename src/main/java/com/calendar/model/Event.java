package com.calendar.model;

import com.calendar.validation.EndTimeAfterStartTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

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
@EndTimeAfterStartTime
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EventID")
    private Integer id;

    @NotBlank(message = "Event title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Description")
    private String description;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Column(name = "Location")
    private String location;

    @NotNull(message = "Start time is required")
    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "EndTime", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "IsAllDay")
    private Boolean isAllDay = false;

    @Size(max = 20, message = "Recurrence type must not exceed 20 characters")
    @Column(name = "RecurrenceType")
    private String recurrenceType;

    @Column(name = "RecurrenceEnd")
    private LocalDateTime recurrenceEnd;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    @Size(max = 10, message = "Priority must not exceed 10 characters")
    @Column(name = "Priority")
    private String priority = "MEDIUM";

    @Size(max = 20, message = "Color must not exceed 20 characters")
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

    @Transient
    private Map<Integer, String> userPermissions = new HashMap<>();

    @Transient
    private List<Integer> reminderMinutes = new ArrayList<>();

    public Integer getId() {return id;}
    public String getTitle() {return title;}
    public String getDescription() {return description;}
    public String getLocation() {return location;}
    public LocalDateTime getStartTime() {return startTime;}
    public LocalDateTime getEndTime() {return endTime;}
    public Boolean getIsAllDay() {return isAllDay;}
    public String getRecurrenceType() {return recurrenceType;}
    public LocalDateTime getRecurrenceEnd() {return recurrenceEnd;}
    public User getUser() {return user;}
    public String getPriority() {return priority;}
    public String getColor() {return color;}
    public Boolean getIsShared() {return isShared;}
    public Set<User> getSharedWith() {return sharedWith;}
    public Map<Integer, String> getUserPermissions() {return userPermissions;}
    public List<Integer> getReminderMinutes() {return reminderMinutes;}


    public void setId(Integer id) {this.id = id;}
    public void setTitle(String title) {this.title = title;}
    public void setDescription(String description) {this.description = description;}
    public void setLocation(String location) {this.location = location;}
    public void setStartTime(LocalDateTime startTime) {this.startTime = startTime;}
    public void setEndTime(LocalDateTime endTime) {this.endTime = endTime;}
    public void setIsAllDay(Boolean allDay) {isAllDay = allDay;}
    public void setRecurrenceType(String recurrenceType) {this.recurrenceType = recurrenceType;}
    public void setRecurrenceEnd(LocalDateTime recurrenceEnd) {this.recurrenceEnd = recurrenceEnd;}
    public void setUser(User user) {this.user = user;}
    public void setPriority(String priority) {this.priority = priority;}
    public void setColor(String color) {this.color = color;}
    public void setIsShared(Boolean shared) {isShared = shared;}
    public void setSharedWith(Set<User> sharedWith) {this.sharedWith = sharedWith;}
    public void setUserPermissions(Map<Integer, String> userPermissions) {this.userPermissions = userPermissions;}
    public void setReminderMinutes(List<Integer> reminderMinutes) {this.reminderMinutes = reminderMinutes;}

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