package com.calendar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Integer id;

    @NotNull(message = "User is required")
    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "EventID")
    private Event event;

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    @Column(name = "Message", nullable = false)
    private String message;

    @Size(max = 50, message = "Type must not exceed 50 characters")
    @Column(name = "Type")
    private String type;

    @Column(name = "IsRead")
    private Boolean isRead = false;

    @NotNull(message = "Created at timestamp is required")
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ReadAt")
    private LocalDateTime readAt;


    public Integer getId() {return id;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public User getUser() {return user;}

    public String getMessage() {return message;}

    public Event getEvent() {return event;}

    public String getType() {return type;}

    public Boolean getRead() {return isRead;}

    public LocalDateTime getReadAt() {return readAt;}


    public void setId(Integer id) {this.id = id;}

    public void setUser(User user) {this.user = user;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public void setEvent(Event event) {this.event = event;}

    public void setType(String type) {this.type = type;}

    public void setRead(Boolean isRead) {this.isRead = isRead;}

    public void setMessage(String message) {this.message = message;}

    public void setReadAt(LocalDateTime readAt) {this.readAt = readAt;}
}