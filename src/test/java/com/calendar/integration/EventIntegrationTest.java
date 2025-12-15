package com.calendar.integration;

import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.repository.EventRepository;
import com.calendar.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integračné testy pre Event API.
 *
 * <p>Tieto testy testujú celý flow od HTTP requestu cez controller,
 * service až po databázu a späť. Používajú reálny Spring kontext
 * a in-memory databázu.</p>
 *
 * <p>Na rozdiel od unit testov v EventControllerTest, tieto testy
 * overujú skutočnú integráciu všetkých vrstiev aplikácie.</p>
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Event API Integration Tests")
class EventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Vyčistíme databázu pred každým testom
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Vytvoríme testovacieho používateľa
        testUser = new User();
        testUser.setUsername("integrationtest");
        testUser.setEmail("integration@test.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Celý CRUD flow pre udalosť")
    void fullEventCrudFlow() throws Exception {
        // 1. CREATE - Vytvorenie udalosti
        Event newEvent = new Event();
        newEvent.setTitle("Integration Test Event");
        newEvent.setDescription("Test Description");
        newEvent.setStartTime(LocalDateTime.of(2024, 12, 20, 10, 0));
        newEvent.setEndTime(LocalDateTime.of(2024, 12, 20, 11, 0));
        newEvent.setIsAllDay(false);
        newEvent.setPriority("HIGH");

        String createResponse = mockMvc.perform(post("/api/events")
                        .param("userId", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Event"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Event createdEvent = objectMapper.readValue(createResponse, Event.class);
        Integer eventId = createdEvent.getId();

        // 2. READ - Získanie udalosti
        mockMvc.perform(get("/api/events")
                        .param("userId", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownedEvents", hasSize(1)))
                .andExpect(jsonPath("$.ownedEvents[0].title").value("Integration Test Event"));

        // 3. UPDATE - Aktualizácia udalosti
        createdEvent.setTitle("Updated Integration Test Event");
        createdEvent.setPriority("LOW");

        mockMvc.perform(put("/api/events/" + eventId)
                        .param("userId", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Test Event"))
                .andExpect(jsonPath("$.priority").value("LOW"));

        // 4. DELETE - Vymazanie udalosti
        mockMvc.perform(delete("/api/events/" + eventId)
                        .param("userId", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event deleted successfully"));

        // 5. VERIFY - Overenie, že udalosť je vymazaná
        mockMvc.perform(get("/api/events")
                        .param("userId", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownedEvents", hasSize(0)));
    }

    @Test
    @DisplayName("Vytvorenie a získanie viacerých udalostí")
    void createAndGetMultipleEvents() throws Exception {
        // Vytvoríme 3 udalosti
        for (int i = 1; i <= 3; i++) {
            Event event = new Event();
            event.setTitle("Event " + i);
            event.setDescription("Description " + i);
            event.setStartTime(LocalDateTime.of(2024, 12, 20, 10 + i, 0));
            event.setEndTime(LocalDateTime.of(2024, 12, 20, 11 + i, 0));
            event.setIsAllDay(false);

            mockMvc.perform(post("/api/events")
                            .param("userId", testUser.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(event)))
                    .andExpect(status().isCreated());
        }

        // Overíme, že všetky 3 udalosti sú v databáze
        mockMvc.perform(get("/api/events")
                        .param("userId", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownedEvents", hasSize(3)))
                .andExpect(jsonPath("$.ownedEvents[0].title").value("Event 1"))
                .andExpect(jsonPath("$.ownedEvents[1].title").value("Event 2"))
                .andExpect(jsonPath("$.ownedEvents[2].title").value("Event 3"));
    }

    @Test
    @DisplayName("Filtrovanie udalostí podľa dátumu")
    void filterEventsByDate() throws Exception {
        // Vytvoríme udalosti v rôznych dátumoch
        Event event1 = new Event();
        event1.setTitle("December Event");
        event1.setStartTime(LocalDateTime.of(2024, 12, 15, 10, 0));
        event1.setEndTime(LocalDateTime.of(2024, 12, 15, 11, 0));
        event1.setIsAllDay(false);

        Event event2 = new Event();
        event2.setTitle("January Event");
        event2.setStartTime(LocalDateTime.of(2025, 1, 15, 10, 0));
        event2.setEndTime(LocalDateTime.of(2025, 1, 15, 11, 0));
        event2.setIsAllDay(false);

        mockMvc.perform(post("/api/events")
                        .param("userId", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/events")
                        .param("userId", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event2)))
                .andExpect(status().isCreated());

        // Filtrujeme len December udalosti
        mockMvc.perform(get("/api/events")
                        .param("userId", testUser.getId().toString())
                        .param("start", "2024-12-01T00:00:00")
                        .param("end", "2024-12-31T23:59:59")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownedEvents", hasSize(1)))
                .andExpect(jsonPath("$.ownedEvents[0].title").value("December Event"));
    }

    @Test
    @DisplayName("Validácia - koniec pred začiatkom")
    void validateEndTimeBeforeStartTime() throws Exception {
        Event invalidEvent = new Event();
        invalidEvent.setTitle("Invalid Event");
        invalidEvent.setStartTime(LocalDateTime.of(2024, 12, 20, 11, 0));
        invalidEvent.setEndTime(LocalDateTime.of(2024, 12, 20, 10, 0)); // Koniec PRED začiatkom!
        invalidEvent.setIsAllDay(false);

        mockMvc.perform(post("/api/events")
                        .param("userId", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEvent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Chyba - neexistujúci používateľ")
    void createEventWithNonExistentUser() throws Exception {
        Event event = new Event();
        event.setTitle("Test Event");
        event.setStartTime(LocalDateTime.of(2024, 12, 20, 10, 0));
        event.setEndTime(LocalDateTime.of(2024, 12, 20, 11, 0));
        event.setIsAllDay(false);

        mockMvc.perform(post("/api/events")
                        .param("userId", "999999") // Neexistujúci user
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Vymazanie neexistujúcej udalosti - idempotentná operácia")
    void deleteNonExistentEvent() throws Exception {
        // API vracia 200 OK aj keď udalosť neexistuje (idempotentná operácia)
        // DELETE by mal byť idempotentný - opakované volania nemenia stav
        mockMvc.perform(delete("/api/events/999999")
                        .param("userId", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // Očakáva 200 OK
    }

    @Test
    @DisplayName("Persistencia údajov v databáze")
    void dataPersistence() throws Exception {
        // Vytvoríme udalosť
        Event event = new Event();
        event.setTitle("Persistence Test");
        event.setStartTime(LocalDateTime.of(2024, 12, 20, 10, 0));
        event.setEndTime(LocalDateTime.of(2024, 12, 20, 11, 0));
        event.setIsAllDay(false);

        String response = mockMvc.perform(post("/api/events")
                        .param("userId", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Event createdEvent = objectMapper.readValue(response, Event.class);

        // Overíme v databáze
        Event dbEvent = eventRepository.findById(createdEvent.getId()).orElse(null);
        assert dbEvent != null;
        assert dbEvent.getTitle().equals("Persistence Test");
        assert dbEvent.getUser().getId().equals(testUser.getId());
    }
}