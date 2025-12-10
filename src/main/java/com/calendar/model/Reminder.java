package com.calendar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminder")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReminderID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "EventID", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "ReminderTime", nullable = false)
    private LocalDateTime reminderTime;

    @Column(name = "MinutesBeforeEvent", nullable = false)
    private Integer minutesBeforeEvent;

    @Column(name = "IsSent", nullable = false)
    private Boolean isSent = false;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "SentAt")
    private LocalDateTime sentAt;

    @Column(name = "ReminderType")
    private String reminderType = "EVENT_START";

    @Column(name = "Message", length = 500)
    private String message;

    // Constructors
    public Reminder() {
        this.createdAt = LocalDateTime.now();
        this.isSent = false;
    }

    public Reminder(Event event, User user, Integer minutesBeforeEvent) {
        this();
        this.event = event;
        this.user = user;
        this.minutesBeforeEvent = minutesBeforeEvent;
        this.reminderTime = event.getStartTime().minusMinutes(minutesBeforeEvent);
        this.message = "Event \"" + event.getTitle() + "\" starts in " + minutesBeforeEvent + " minutes";
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
        if (event != null && this.minutesBeforeEvent != null) {
            this.reminderTime = event.getStartTime().minusMinutes(this.minutesBeforeEvent);
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public Integer getMinutesBeforeEvent() {
        return minutesBeforeEvent;
    }

    public void setMinutesBeforeEvent(Integer minutesBeforeEvent) {
        this.minutesBeforeEvent = minutesBeforeEvent;
        if (this.event != null) {
            this.reminderTime = this.event.getStartTime().minusMinutes(minutesBeforeEvent);
        }
    }

    public Boolean getIsSent() {
        return isSent;
    }

    public void setIsSent(Boolean isSent) {
        this.isSent = isSent;
        if (isSent && this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getReminderType() {
        return reminderType;
    }

    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id=" + id +
                ", reminderTime=" + reminderTime +
                ", minutesBeforeEvent=" + minutesBeforeEvent +
                ", isSent=" + isSent +
                ", message='" + message + '\'' +
                '}';
    }
}