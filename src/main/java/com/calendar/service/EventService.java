package com.calendar.service;

import com.calendar.model.Event;
import com.calendar.model.Reminder;
import com.calendar.model.User;
import com.calendar.repository.EventRepository;
import com.calendar.repository.ReminderRepository;
import com.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Servisná vrstva pre správu udalostí a pripomienok.
 *
 * <p>EventService poskytuje komplexnú business logiku pre prácu s udalosťami,
 * vrátane ich vytvárania, aktualizácie, mazania, zdieľania a správy pripomienok.</p>
 *
 * <p>Hlavné funkcie:
 * <ul>
 *   <li>CRUD operácie s udalosťami</li>
 *   <li>Správa zdieľaných udalostí a oprávnení</li>
 *   <li>Správa pripomienok používateľov</li>
 *   <li>Vyhľadávanie udalostí podľa rôznych kritérií</li>
 *   <li>Administrátorské funkcie</li>
 * </ul></p>
 *
 */
@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Pomocná metóda na obohatenie zoznamu udalostí o oprávnenia z databázy.
     * Pre každú udalosť načíta mapu oprávnení používateľov z join tabuľky.
     *
     * @param events zoznam udalostí na obohatenie
     */
    private void enrichEventsWithPermissions(List<Event> events) {
        for (Event event : events) {
            enrichEventWithPermissions(event);
        }
    }

    /**
     * Pomocná metóda na obohatenie jednej udalosti o oprávnenia z databázy.
     * Ak je udalosť zdieľaná, načíta oprávnenia všetkých používateľov s prístupom.
     *
     * @param event udalosť na obohatenie
     */
    private void enrichEventWithPermissions(Event event) {
        if (event.getIsShared() && event.getId() != null) {
            Map<Integer, String> permissions = getEventPermissions(event.getId());
            event.setUserPermissions(permissions);
        } else {
            event.setUserPermissions(new HashMap<>());
        }
    }

    /**
     * Pomocná metóda na načítanie pripomienok pre udalosť a používateľa.
     * Nastaví zoznam minút pred udalosťou do transientného poľa Event.reminderMinutes.
     *
     * @param event udalosť, pre ktorú sa načítavajú pripomienky
     * @param userId ID používateľa
     */
    private void loadReminderMinutes(Event event, Integer userId) {
        if (event.getId() != null && userId != null) {
            List<Reminder> reminders = reminderRepository.findByEventIdAndUserId(event.getId(), userId);
            List<Integer> minutes = new ArrayList<>();
            for (Reminder reminder : reminders) {
                minutes.add(reminder.getMinutesBeforeEvent());
            }
            event.setReminderMinutes(minutes);
        }
    }

    /**
     * Pomocná metóda na načítanie pripomienok pre viacero udalostí.
     *
     * @param events zoznam udalostí
     * @param userId ID používateľa
     */
    private void loadReminderMinutesForEvents(List<Event> events, Integer userId) {
        for (Event event : events) {
            loadReminderMinutes(event, userId);
        }
    }

    /**
     * Pomocná metóda na uloženie pripomienok pre udalosť.
     * Vymaže existujúce pripomienky používateľa pre danú udalosť
     * a vytvorí nové podľa poskytnutého zoznamu minút.
     *
     * @param event udalosť, pre ktorú sa vytvárajú pripomienky
     * @param userId ID používateľa
     * @param minutesList zoznam minút pred udalosťou (napr. [15, 60, 1440])
     */
    @Transactional
    private void saveReminders(Event event, Integer userId, List<Integer> minutesList) {
        if (minutesList == null || minutesList.isEmpty()) {
            return;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return;
        }

        User user = userOpt.get();

        // Delete existing reminders for this event and user
        List<Reminder> existingReminders = reminderRepository.findByEventIdAndUserId(event.getId(), userId);
        reminderRepository.deleteAll(existingReminders);

        // Create new reminders
        for (Integer minutesBefore : minutesList) {
            Reminder reminder = new Reminder(event, user, minutesBefore);
            reminderRepository.save(reminder);
        }
    }

    /**
     * Vytvorí novú udalosť pre zadaného používateľa.
     * Automaticky nastaví vlastníka udalosti a uloží pripomienky, ak sú poskytnuté.
     *
     * @param event udalosť na vytvorenie (bez ID)
     * @param userId ID vlastníka udalosti
     * @return uložená udalosť s vygenerovaným ID a načítanými oprávneniami
     * @throws RuntimeException ak používateľ neexistuje
     */
    @Transactional
    public Event createEvent(Event event, Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            event.setUser(userOptional.get());
            Event savedEvent = eventRepository.save(event);
            enrichEventWithPermissions(savedEvent);

            // Save reminders if provided
            if (event.getReminderMinutes() != null && !event.getReminderMinutes().isEmpty()) {
                saveReminders(savedEvent, userId, event.getReminderMinutes());
                savedEvent.setReminderMinutes(event.getReminderMinutes());
            }

            return savedEvent;
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    /**
     * Vráti všetky vlastné udalosti používateľa (bez zdieľaných).
     *
     * @param userId ID používateľa
     * @return zoznam vlastných udalostí s oprávneniami a pripomienkami
     */
    public List<Event> getUserEvents(Integer userId) {
        List<Event> events = eventRepository.findByUserId(userId);
        enrichEventsWithPermissions(events);
        loadReminderMinutesForEvents(events, userId);
        return events;
    }

    /**
     * Vráti všetky udalosti používateľa - vlastné aj zdieľané.
     *
     * @param userId ID používateľa
     * @return kompletný zoznam dostupných udalostí
     */
    public List<Event> getAllUserEvents(Integer userId) {
        List<Event> events = eventRepository.findByUserIdOrSharedWithUser(userId);
        enrichEventsWithPermissions(events);
        loadReminderMinutesForEvents(events, userId);
        return events;
    }

    /**
     * Vráti vlastné udalosti používateľa v zadanom časovom rozsahu.
     *
     * @param userId ID používateľa
     * @param start začiatok časového rozsahu
     * @param end koniec časového rozsahu
     * @return filtrovaný zoznam udalostí
     */
    public List<Event> getUserEventsBetweenDates(Integer userId, LocalDateTime start, LocalDateTime end) {
        List<Event> events = eventRepository.findByUserIdAndStartTimeBetween(userId, start, end);
        enrichEventsWithPermissions(events);
        loadReminderMinutesForEvents(events, userId);
        return events;
    }

    /**
     * Vráti všetky dostupné udalosti (vlastné + zdieľané) v zadanom rozsahu.
     *
     * @param userId ID používateľa
     * @param start začiatok časového rozsahu
     * @param end koniec časového rozsahu
     * @return filtrovaný zoznam udalostí
     */
    public List<Event> getAllUserEventsBetweenDates(Integer userId, LocalDateTime start, LocalDateTime end) {
        List<Event> events = eventRepository.findByUserIdOrSharedWithUserBetweenDates(userId, start, end);
        enrichEventsWithPermissions(events);
        loadReminderMinutesForEvents(events, userId);
        return events;
    }

    /**
     * Aktualizuje existujúcu udalosť.
     * Aktualizuje všetky polia udalosti, oprávnenia zdieľaných používateľov
     * a pripomienky vlastníka udalosti.
     *
     * @param eventId ID udalosti na aktualizáciu
     * @param eventDetails nové údaje udalosti
     * @return aktualizovaná udalosť s načítanými oprávneniami
     * @throws RuntimeException ak udalosť neexistuje
     */
    @Transactional
    public Event updateEvent(Integer eventId, Event eventDetails) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isPresent()) {
            Event existingEvent = eventOptional.get();
            existingEvent.setTitle(eventDetails.getTitle());
            existingEvent.setDescription(eventDetails.getDescription());
            existingEvent.setLocation(eventDetails.getLocation());
            existingEvent.setStartTime(eventDetails.getStartTime());
            existingEvent.setEndTime(eventDetails.getEndTime());
            existingEvent.setIsAllDay(eventDetails.getIsAllDay());
            existingEvent.setRecurrenceType(eventDetails.getRecurrenceType());
            existingEvent.setRecurrenceEnd(eventDetails.getRecurrenceEnd());
            existingEvent.setPriority(eventDetails.getPriority());
            existingEvent.setColor(eventDetails.getColor());
            existingEvent.setIsShared(eventDetails.getIsShared());

            if (eventDetails.getSharedWith() != null) {
                existingEvent.setSharedWith(eventDetails.getSharedWith());
            }

            // Save the event first
            Event savedEvent = eventRepository.save(existingEvent);

            // Flush to ensure join table entries are created
            entityManager.flush();

            // Handle permissions update
            if (eventDetails.getUserPermissions() != null && !eventDetails.getUserPermissions().isEmpty()) {
                for (Map.Entry<Integer, String> entry : eventDetails.getUserPermissions().entrySet()) {
                    Integer userId = entry.getKey();
                    String permission = entry.getValue();

                    boolean userIsShared = savedEvent.getSharedWith().stream()
                            .anyMatch(user -> user.getId().equals(userId));

                    if (userIsShared) {
                        String updateSql = "UPDATE event_shared_users SET permission = ? WHERE event_id = ? AND userPermissions_KEY = ?";
                        int rowsAffected = jdbcTemplate.update(updateSql, permission, eventId, userId);

                        if (rowsAffected == 0) {
                            try {
                                String insertSql = "INSERT INTO event_shared_users (event_id, userPermissions_KEY, permission) VALUES (?, ?, ?)";
                                jdbcTemplate.update(insertSql, eventId, userId, permission);
                            } catch (Exception e) {
                                rowsAffected = jdbcTemplate.update(updateSql, permission, eventId, userId);
                            }
                        }
                    }
                }
            }

            // Handle reminders update (only for the event owner)
            Integer ownerId = savedEvent.getUser().getId();
            if (eventDetails.getReminderMinutes() != null) {
                saveReminders(savedEvent, ownerId, eventDetails.getReminderMinutes());
                savedEvent.setReminderMinutes(eventDetails.getReminderMinutes());
            }

            // Enrich the event with permissions before returning
            enrichEventWithPermissions(savedEvent);
            return savedEvent;
        } else {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }
    }

    /**
     * Vymaže udalosť podľa ID.
     * Automaticky sa vymažú všetky pripomienky a oprávnenia spojené s udalosťou.
     *
     * @param eventId ID udalosti na vymazanie
     */
    public void deleteEvent(Integer eventId) {
        eventRepository.deleteById(eventId);
    }

    /**
     * Vráti všetky udalosti zdieľané s daným používateľom.
     *
     * @param userId ID používateľa
     * @return zoznam zdieľaných udalostí
     */
    public List<Event> getSharedEvents(Integer userId) {
        List<Event> events = eventRepository.findSharedWithUser(userId);
        enrichEventsWithPermissions(events);
        loadReminderMinutesForEvents(events, userId);
        return events;
    }

    /**
     * Vráti zdieľané udalosti v zadanom časovom rozsahu.
     *
     * @param userId ID používateľa
     * @param start začiatok časového rozsahu
     * @param end koniec časového rozsahu
     * @return filtrovaný zoznam zdieľaných udalostí
     */
    public List<Event> getSharedEventsBetweenDates(Integer userId, LocalDateTime start, LocalDateTime end) {
        List<Event> events = eventRepository.findSharedWithUserBetweenDates(userId, start, end);
        enrichEventsWithPermissions(events);
        loadReminderMinutesForEvents(events, userId);
        return events;
    }

    /**
     * Vráti čakajúce pripomienky používateľa (pripomienky, ktoré majú byť zobrazené).
     * Získa pripomienky, ktoré ešte neboli odoslané a ich čas už nastal.
     *
     * @param userId ID používateľa
     * @return zoznam pripomienok na odoslanie
     */
    public List<Reminder> getPendingReminders(Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        return reminderRepository.findPendingRemindersForUser(userId, now);
    }

    /**
     * Označí pripomienku ako odoslanú.
     * Automaticky nastaví čas odoslania na aktuálny čas.
     *
     * @param reminderId ID pripomienky
     */
    @Transactional
    public void markReminderAsSent(Integer reminderId) {
        Optional<Reminder> reminderOpt = reminderRepository.findById(reminderId);
        if (reminderOpt.isPresent()) {
            Reminder reminder = reminderOpt.get();
            reminder.setIsSent(true);
            reminder.setSentAt(LocalDateTime.now());
            reminderRepository.save(reminder);
        }
    }

    /**
     * Vráti mapu oprávnení používateľov pre danú udalosť.
     * Načíta údaje priamo z join tabuľky event_shared_users pomocou JDBC.
     *
     * @param eventId ID udalosti
     * @return mapa: UserID -> Permission (napr. "VIEW", "EDIT", "ADMIN")
     */
    public Map<Integer, String> getEventPermissions(Integer eventId) {
        String sql = "SELECT userPermissions_KEY, permission FROM event_shared_users WHERE event_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, eventId);

        Map<Integer, String> permissions = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer userId = ((Number) row.get("userPermissions_KEY")).intValue();
            String permission = (String) row.get("permission");
            permissions.put(userId, permission);
        }

        return permissions;
    }

    // ==================== ADMIN METHODS ====================

    /**
     * Vráti všetky pripomienky v systéme.
     * Používa sa v admin paneli na prehľad všetkých pripomienok.
     *
     * @return zoznam všetkých pripomienok
     */
    public List<Reminder> getAllReminders() {
        return reminderRepository.findAll();
    }

    /**
     * Vymaže pripomienku podľa ID.
     * Používa sa v admin paneli.
     *
     * @param reminderId ID pripomienky na vymazanie
     * @throws RuntimeException ak pripomienka neexistuje
     */
    @Transactional
    public void deleteReminder(Integer reminderId) {
        if (!reminderRepository.existsById(reminderId)) {
            throw new RuntimeException("Reminder not found with ID: " + reminderId);
        }
        reminderRepository.deleteById(reminderId);
    }
}