package com.calendar.model;

import com.calendar.validation.EndTimeAfterStartTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Entita reprezentujúca udalosť v kalendári.
 *
 * <p>Udalosť je hlavná entita aplikácie, ktorá obsahuje všetky informácie
 * o kalendárnej udalosti: názov, čas, miesto, popis, prioritu a možnosť
 * zdieľania s inými používateľmi.</p>
 *
 * <p>Každá udalosť patrí jednému vlastníkovi (User) a môže byť zdieľaná
 * s ďalšími používateľmi s definovanými právami (VIEW, EDIT, ADMIN).</p>
 *
 * <p>Udalosť podporuje:
 * <ul>
 *   <li>Celodňové udalosti</li>
 *   <li>Opakovanie (daily, weekly, monthly, yearly)</li>
 *   <li>Pripomienky v rôznych časových intervaloch</li>
 *   <li>Farebné kategórie</li>
 *   <li>Priority (HIGH, MEDIUM, LOW)</li>
 *   <li>Zdieľanie s inými používateľmi</li>
 * </ul></p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event")
@EndTimeAfterStartTime
public class Event {

    /**
     * Unikátny identifikátor udalosti (primárny kľúč).
     * Automaticky generovaný pri vytvorení novej udalosti.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EventID")
    private Integer id;

    /**
     * Názov udalosti.
     * Povinné pole, dĺžka 1-255 znakov.
     * Napríklad: "Team meeting", "Doctor appointment"
     */
    @NotBlank(message = "Event title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Column(name = "Title", nullable = false)
    private String title;

    /**
     * Podrobný popis udalosti.
     * Voliteľné pole pre dodatočné informácie.
     */
    @Column(name = "Description")
    private String description;

    /**
     * Miesto konania udalosti.
     * Maximálna dĺžka 255 znakov.
     * Napríklad: "Conference Room A", "123 Main St"
     */
    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Column(name = "Location")
    private String location;

    /**
     * Čas začiatku udalosti.
     * Povinné pole. Musí byť pred časom ukončenia.
     */
    @NotNull(message = "Start time is required")
    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;

    /**
     * Čas ukončenia udalosti.
     * Povinné pole. Musí byť po čase začiatku (validované @EndTimeAfterStartTime).
     */
    @NotNull(message = "End time is required")
    @Column(name = "EndTime", nullable = false)
    private LocalDateTime endTime;

    /**
     * Označenie, či je udalosť celodňová.
     * Predvolená hodnota: false
     */
    @Column(name = "IsAllDay")
    private Boolean isAllDay = false;

    /**
     * Typ opakovania udalosti.
     * Možné hodnoty: "daily", "weekly", "monthly", "yearly"
     * Maximálna dĺžka 20 znakov.
     */
    @Size(max = 20, message = "Recurrence type must not exceed 20 characters")
    @Column(name = "RecurrenceType")
    private String recurrenceType;

    /**
     * Dátum ukončenia opakovania udalosti.
     * Ak je nastavený, udalosť sa prestane opakovať po tomto dátume.
     */
    @Column(name = "RecurrenceEnd")
    private LocalDateTime recurrenceEnd;

    /**
     * Vlastník udalosti (používateľ, ktorý udalosť vytvoril).
     * Vzťah Many-to-One (viac udalostí môže patriť jednému používateľovi).
     */
    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    /**
     * Priorita udalosti.
     * Možné hodnoty: "HIGH", "MEDIUM", "LOW"
     * Predvolená hodnota: "MEDIUM"
     * Maximálna dĺžka 10 znakov.
     */
    @Size(max = 10, message = "Priority must not exceed 10 characters")
    @Column(name = "Priority")
    private String priority = "MEDIUM";

    /**
     * Farba udalosti v hexadecimálnom formáte.
     * Používa sa na vizuálne odlíšenie v kalendári.
     * Maximálna dĺžka 20 znakov (napr. "#3788d8").
     */
    @Size(max = 20, message = "Color must not exceed 20 characters")
    @Column(name = "Color")
    private String color;

    /**
     * Označenie, či je udalosť zdieľaná s inými používateľmi.
     * Predvolená hodnota: false
     */
    @Column(name = "IsShared")
    private Boolean isShared = false;

    /**
     * Zoznam používateľov, s ktorými je udalosť zdieľaná.
     * Vzťah Many-to-Many (udalosť môže byť zdieľaná s viacerými používateľmi).
     */
    @ManyToMany
    @JoinTable(
            name = "event_shared_users",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "userPermissions_KEY")
    )
    private Set<User> sharedWith = new HashSet<>();

    /**
     * Mapa oprávnení jednotlivých používateľov k tejto udalosti.
     * Kľúč: UserID, Hodnota: Typ oprávnenia ("VIEW", "EDIT", "ADMIN")
     * Transientné pole (neukladá sa do databázy).
     */
    @Transient
    private Map<Integer, String> userPermissions = new HashMap<>();

    /**
     * Zoznam pripomienok v minútach pred udalosťou.
     * Napríklad: [15, 60, 1440] = pripomienky 15 min, 1 hod a 1 deň pred udalosťou.
     * Transientné pole (neukladá sa do databázy).
     */
    @Transient
    private List<Integer> reminderMinutes = new ArrayList<>();


    /** @return ID udalosti */
    public Integer getId() {return id;}

    /** @return názov udalosti */
    public String getTitle() {return title;}

    /** @return popis udalosti */
    public String getDescription() {return description;}

    /** @return miesto konania */
    public String getLocation() {return location;}

    /** @return čas začiatku */
    public LocalDateTime getStartTime() {return startTime;}

    /** @return čas ukončenia */
    public LocalDateTime getEndTime() {return endTime;}

    /** @return true ak je celodňová udalosť */
    public Boolean getIsAllDay() {return isAllDay;}

    /** @return typ opakovania */
    public String getRecurrenceType() {return recurrenceType;}

    /** @return dátum ukončenia opakovania */
    public LocalDateTime getRecurrenceEnd() {return recurrenceEnd;}

    /** @return vlastník udalosti */
    public User getUser() {return user;}

    /** @return priorita udalosti */
    public String getPriority() {return priority;}

    /** @return farba udalosti */
    public String getColor() {return color;}

    /** @return true ak je udalosť zdieľaná */
    public Boolean getIsShared() {return isShared;}

    /** @return zoznam používateľov s prístupom */
    public Set<User> getSharedWith() {return sharedWith;}

    /** @return mapa oprávnení používateľov */
    public Map<Integer, String> getUserPermissions() {return userPermissions;}

    /** @return zoznam pripomienok v minútach */
    public List<Integer> getReminderMinutes() {return reminderMinutes;}


    /** @param id nové ID udalosti */
    public void setId(Integer id) {this.id = id;}

    /** @param title nový názov udalosti */
    public void setTitle(String title) {this.title = title;}

    /** @param description nový popis */
    public void setDescription(String description) {this.description = description;}

    /** @param location nové miesto konania */
    public void setLocation(String location) {this.location = location;}

    /** @param startTime nový čas začiatku */
    public void setStartTime(LocalDateTime startTime) {this.startTime = startTime;}

    /** @param endTime nový čas ukončenia */
    public void setEndTime(LocalDateTime endTime) {this.endTime = endTime;}

    /** @param allDay true pre celodňovú udalosť */
    public void setIsAllDay(Boolean allDay) {isAllDay = allDay;}

    /** @param recurrenceType nový typ opakovania */
    public void setRecurrenceType(String recurrenceType) {this.recurrenceType = recurrenceType;}

    /** @param recurrenceEnd nový dátum ukončenia opakovania */
    public void setRecurrenceEnd(LocalDateTime recurrenceEnd) {this.recurrenceEnd = recurrenceEnd;}

    /** @param user nový vlastník */
    public void setUser(User user) {this.user = user;}

    /** @param priority nová priorita */
    public void setPriority(String priority) {this.priority = priority;}

    /** @param color nová farba */
    public void setColor(String color) {this.color = color;}

    /** @param shared true ak je zdieľaná */
    public void setIsShared(Boolean shared) {isShared = shared;}

    /** @param sharedWith nový zoznam zdieľaných používateľov */
    public void setSharedWith(Set<User> sharedWith) {this.sharedWith = sharedWith;}

    /** @param userPermissions nová mapa oprávnení */
    public void setUserPermissions(Map<Integer, String> userPermissions) {this.userPermissions = userPermissions;}

    /** @param reminderMinutes nový zoznam pripomienok */
    public void setReminderMinutes(List<Integer> reminderMinutes) {this.reminderMinutes = reminderMinutes;}

    /**
     * Zdieľa udalosť s konkrétnym používateľom.
     * Automaticky nastaví isShared na true.
     *
     * @param user používateľ, s ktorým sa má udalosť zdieľať
     */
    public void shareWithUser(User user) {
        this.sharedWith.add(user);
        this.isShared = true;
    }

    /**
     * Odstráni používateľa zo zdieľania udalosti.
     * Ak neostanú žiadni zdieľaní používatelia, nastaví isShared na false.
     *
     * @param user používateľ, ktorému sa má odobrať prístup
     */
    public void removeSharedUser(User user) {
        this.sharedWith.remove(user);
        if (this.sharedWith.isEmpty()) {
            this.isShared = false;
        }
    }
}