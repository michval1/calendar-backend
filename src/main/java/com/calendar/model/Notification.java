package com.calendar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entita reprezentujúca notifikáciu pre používateľa.
 *
 * <p>Notifikácie informujú používateľov o dôležitých udalostiach v aplikácii,
 * ako sú pripomienky udalostí, pozvánky na zdieľané udalosti, alebo zmeny
 * v udalostiach.</p>
 *
 * <p>Každá notifikácia je priradená konkrétnemu používateľovi a volitelne
 * môže byť spojená s udalosťou. Notifikácia môže byť prečítaná alebo
 * neprečítaná.</p>
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification")
public class Notification {

    /**
     * Unikátny identifikátor notifikácie (primárny kľúč).
     * Automaticky generovaný pri vytvorení novej notifikácie.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Integer id;

    /**
     * Používateľ, pre ktorého je notifikácia určená.
     * Povinné pole - každá notifikácia musí mať príjemcu.
     */
    @NotNull(message = "User is required")
    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    /**
     * Udalosť spojená s touto notifikáciou.
     * Voliteľné - niektoré notifikácie nemusia byť spojené s konkrétnou udalosťou.
     */
    @ManyToOne
    @JoinColumn(name = "EventID")
    private Event event;

    /**
     * Text notifikačnej správy.
     * Povinné pole, maximálna dĺžka 500 znakov.
     * Napríklad: "Pripomienka: Meeting with team o 15 minút"
     */
    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    @Column(name = "Message", nullable = false)
    private String message;

    /**
     * Typ notifikácie.
     * Možné hodnoty: "REMINDER", "SHARE_INVITE", "EVENT_UPDATE", atď.
     * Maximálna dĺžka 50 znakov.
     */
    @Size(max = 50, message = "Type must not exceed 50 characters")
    @Column(name = "Type")
    private String type;

    /**
     * Označenie, či bola notifikácia prečítaná.
     * Predvolená hodnota: false (neprečítaná)
     */
    @Column(name = "IsRead")
    private Boolean isRead = false;

    /**
     * Časová značka vytvorenia notifikácie.
     * Automaticky sa nastaví pri vytvorení notifikácie.
     * Povinné pole.
     */
    @NotNull(message = "Created at timestamp is required")
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Časová značka prečítania notifikácie.
     * Nastaví sa automaticky, keď používateľ označí notifikáciu ako prečítanú.
     */
    @Column(name = "ReadAt")
    private LocalDateTime readAt;


    /** @return ID notifikácie */
    public Integer getId() {return id;}

    /** @return čas vytvorenia notifikácie */
    public LocalDateTime getCreatedAt() {return createdAt;}

    /** @return používateľ, ktorému je notifikácia určená */
    public User getUser() {return user;}

    /** @return text notifikačnej správy */
    public String getMessage() {return message;}

    /** @return udalosť spojená s notifikáciou */
    public Event getEvent() {return event;}

    /** @return typ notifikácie */
    public String getType() {return type;}

    /** @return true ak bola notifikácia prečítaná */
    public Boolean getRead() {return isRead;}

    /** @return čas prečítania notifikácie */
    public LocalDateTime getReadAt() {return readAt;}


    /** @param id nové ID notifikácie */
    public void setId(Integer id) {this.id = id;}

    /** @param user nový príjemca notifikácie */
    public void setUser(User user) {this.user = user;}

    /** @param createdAt nový čas vytvorenia */
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    /** @param event nová spojená udalosť */
    public void setEvent(Event event) {this.event = event;}

    /** @param type nový typ notifikácie */
    public void setType(String type) {this.type = type;}

    /** @param isRead nový stav prečítania */
    public void setRead(Boolean isRead) {this.isRead = isRead;}

    /** @param message nový text správy */
    public void setMessage(String message) {this.message = message;}

    /** @param readAt nový čas prečítania */
    public void setReadAt(LocalDateTime readAt) {this.readAt = readAt;}
}