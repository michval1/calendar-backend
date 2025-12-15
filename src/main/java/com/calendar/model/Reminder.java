package com.calendar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entita reprezentujúca pripomienku k udalosti.
 *
 * <p>Pripomienka slúži na upozornenie používateľa pred začiatkom udalosti.
 * Každá pripomienka je spojená s konkrétnou udalosťou a používateľom,
 * a obsahuje informáciu o čase, kedy má byť odoslaná.</p>
 *
 * <p>Pripomienky sa automaticky generujú na základe nastavenia udalosti
 * (napríklad 15 minút, 1 hodina alebo 1 deň pred začiatkom udalosti).</p>
 *
 */
@Entity
@Table(name = "reminder")
public class Reminder {

    /**
     * Unikátny identifikátor pripomienky (primárny kľúč).
     * Automaticky generovaný pri vytvorení novej pripomienky.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReminderID")
    private Integer id;

    /**
     * Udalosť, ku ktorej sa pripomienka vzťahuje.
     * Povinné pole - každá pripomienka musí byť spojená s udalosťou.
     */
    @ManyToOne
    @JoinColumn(name = "EventID", nullable = false)
    private Event event;

    /**
     * Používateľ, ktorý dostane pripomienku.
     * Povinné pole - každá pripomienka je určená konkrétnemu používateľovi.
     */
    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    /**
     * Presný čas, kedy má byť pripomienka odoslaná.
     * Vypočítava sa automaticky na základe času začiatku udalosti
     * a počtu minút pred udalosťou.
     */
    @Column(name = "ReminderTime", nullable = false)
    private LocalDateTime reminderTime;

    /**
     * Počet minút pred začiatkom udalosti, kedy sa má pripomienka odoslať.
     * Napríklad: 15 = 15 minút pred, 60 = 1 hodina pred, 1440 = 1 deň pred.
     */
    @Column(name = "MinutesBeforeEvent", nullable = false)
    private Integer minutesBeforeEvent;

    /**
     * Označenie, či bola pripomienka už odoslaná.
     * Predvolená hodnota: false (neodoslaná)
     * Po odoslaní sa automaticky nastaví sentAt.
     */
    @Column(name = "IsSent", nullable = false)
    private Boolean isSent = false;

    /**
     * Časová značka vytvorenia pripomienky.
     * Automaticky sa nastaví pri vytvorení novej pripomienky.
     */
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Časová značka odoslania pripomienky.
     * Nastaví sa automaticky pri označení pripomienky ako odoslanej.
     */
    @Column(name = "SentAt")
    private LocalDateTime sentAt;

    /**
     * Typ pripomienky.
     * Predvolená hodnota: "EVENT_START"
     * Môže označovať rôzne typy pripomienok (začiatok udalosti, deadline, atď.)
     */
    @Column(name = "ReminderType")
    private String reminderType = "EVENT_START";

    /**
     * Text pripomienkovej správy.
     * Maximálna dĺžka 500 znakov.
     * Automaticky sa generuje pri vytvorení pripomienky.
     */
    @Column(name = "Message", length = 500)
    private String message;

    /**
     * Predvolený konštruktor.
     * Inicializuje createdAt na aktuálny čas a isSent na false.
     */
    public Reminder() {
        this.createdAt = LocalDateTime.now();
        this.isSent = false;
    }

    /**
     * Konštruktor na vytvorenie pripomienky s parametrami.
     * Automaticky vypočíta čas odoslania pripomienky a vygeneruje správu.
     *
     * @param event udalosť, ku ktorej sa pripomienka vzťahuje
     * @param user používateľ, ktorý dostane pripomienku
     * @param minutesBeforeEvent počet minút pred udalosťou
     */
    public Reminder(Event event, User user, Integer minutesBeforeEvent) {
        this();
        this.event = event;
        this.user = user;
        this.minutesBeforeEvent = minutesBeforeEvent;
        this.reminderTime = event.getStartTime().minusMinutes(minutesBeforeEvent);
        this.message = "Event \"" + event.getTitle() + "\" starts in " + minutesBeforeEvent + " minutes";
    }


    /** @return ID pripomienky */
    public Integer getId() {return id;}

    /** @return udalosť spojená s pripomienkou */
    public Event getEvent() {return event;}

    /** @return používateľ, ktorý dostane pripomienku */
    public User getUser() {return user;}

    /** @return čas odoslania pripomienky */
    public LocalDateTime getReminderTime() {return reminderTime;}

    /** @return počet minút pred udalosťou */
    public Integer getMinutesBeforeEvent() {return minutesBeforeEvent;}

    /** @return true ak bola pripomienka odoslaná */
    public Boolean getIsSent() {return isSent;}

    /** @return čas vytvorenia pripomienky */
    public LocalDateTime getCreatedAt() {return createdAt;}

    /** @return čas odoslania pripomienky */
    public LocalDateTime getSentAt() {return sentAt;}

    /** @return typ pripomienky */
    public String getReminderType() {return reminderType;}

    /** @return text pripomienkovej správy */
    public String getMessage() {return message;}


    /** @param id nové ID pripomienky */
    public void setId(Integer id) {this.id = id;}

    /**
     * Nastaví počet minút pred udalosťou a prepočíta čas odoslania.
     * @param minutesBeforeEvent nový počet minút pred udalosťou
     */
    public void setMinutesBeforeEvent(Integer minutesBeforeEvent) {
        this.minutesBeforeEvent = minutesBeforeEvent;
        if (this.event != null) {
            this.reminderTime = this.event.getStartTime().minusMinutes(minutesBeforeEvent);
        }
    }

    /**
     * Nastaví udalosť a prepočíta čas odoslania pripomienky.
     * @param event nová udalosť
     */
    public void setEvent(Event event) {
        this.event = event;
        if (event != null && this.minutesBeforeEvent != null) {
            this.reminderTime = event.getStartTime().minusMinutes(this.minutesBeforeEvent);
        }
    }

    /** @param user nový používateľ */
    public void setUser(User user) {this.user = user;}

    /** @param reminderTime nový čas odoslania */
    public void setReminderTime(LocalDateTime reminderTime) {this.reminderTime = reminderTime;}

    /**
     * Nastaví stav odoslania pripomienky.
     * Pri nastavení na true automaticky nastaví sentAt na aktuálny čas.
     * @param isSent nový stav odoslania
     */
    public void setIsSent(Boolean isSent) {
        this.isSent = isSent;
        if (isSent && this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }

    /** @param createdAt nový čas vytvorenia */
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    /** @param sentAt nový čas odoslania */
    public void setSentAt(LocalDateTime sentAt) {this.sentAt = sentAt;}

    /** @param reminderType nový typ pripomienky */
    public void setReminderType(String reminderType) {this.reminderType = reminderType;}

    /** @param message nový text správy */
    public void setMessage(String message) {this.message = message;}

    /**
     * Textová reprezentácia pripomienky pre debugging účely.
     * @return textová reprezentácia objektu
     */
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