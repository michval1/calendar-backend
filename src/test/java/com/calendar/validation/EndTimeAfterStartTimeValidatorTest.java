package com.calendar.validation;

import com.calendar.model.Event;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testy pre EndTimeAfterStartTimeValidator.
 * 
 * <p>Testuje validáciu časov udalostí.</p>
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EndTimeAfterStartTimeValidator Unit Tests")
class EndTimeAfterStartTimeValidatorTest {

    private EndTimeAfterStartTimeValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new EndTimeAfterStartTimeValidator();
    }

    @Test
    @DisplayName("Validácia úspešná - endTime je po startTime")
    void isValid_EndTimeAfterStartTime_ReturnsTrue() {
        Event event = new Event();
        event.setStartTime(LocalDateTime.of(2024, 12, 20, 10, 0));
        event.setEndTime(LocalDateTime.of(2024, 12, 20, 11, 0));

        boolean isValid = validator.isValid(event, context);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validácia neúspešná - endTime je pred startTime")
    void isValid_EndTimeBeforeStartTime_ReturnsFalse() {
        Event event = new Event();
        event.setStartTime(LocalDateTime.of(2024, 12, 20, 11, 0));
        event.setEndTime(LocalDateTime.of(2024, 12, 20, 10, 0));

        boolean isValid = validator.isValid(event, context);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validácia neúspešná - rovnaký čas")
    void isValid_SameTime_ReturnsFalse() {
        Event event = new Event();
        LocalDateTime sameTime = LocalDateTime.of(2024, 12, 20, 10, 0);
        event.setStartTime(sameTime);
        event.setEndTime(sameTime);

        boolean isValid = validator.isValid(event, context);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validácia prejde - event je null")
    void isValid_NullEvent_ReturnsTrue() {
        boolean isValid = validator.isValid(null, context);

        assertTrue(isValid, "Validácia by mala prejsť pre null event");
    }

    @Test
    @DisplayName("Validácia prejde - startTime je null")
    void isValid_NullStartTime_ReturnsTrue() {
        Event event = new Event();
        event.setStartTime(null);
        event.setEndTime(LocalDateTime.of(2024, 12, 20, 11, 0));

        boolean isValid = validator.isValid(event, context);

        assertTrue(isValid, "Validácia by mala prejsť ak je startTime null");
    }

    @Test
    @DisplayName("Validácia prejde - endTime je null")
    void isValid_NullEndTime_ReturnsTrue() {
        Event event = new Event();
        event.setStartTime(LocalDateTime.of(2024, 12, 20, 10, 0));
        event.setEndTime(null);

        boolean isValid = validator.isValid(event, context);

        assertTrue(isValid, "Validácia by mala prejsť ak je endTime null");
    }

    @Test
    @DisplayName("Validácia prejde - oba časy sú null")
    void isValid_BothTimesNull_ReturnsTrue() {
        Event event = new Event();
        event.setStartTime(null);
        event.setEndTime(null);

        boolean isValid = validator.isValid(event, context);

        assertTrue(isValid, "Validácia by mala prejsť ak sú oba časy null");
    }

    @Test
    @DisplayName("Validácia úspešná - endTime je ďaleko v budúcnosti")
    void isValid_EndTimeFarInFuture_ReturnsTrue() {
        Event event = new Event();
        event.setStartTime(LocalDateTime.of(2024, 12, 20, 10, 0));
        event.setEndTime(LocalDateTime.of(2025, 12, 20, 10, 0));

        boolean isValid = validator.isValid(event, context);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validácia úspešná - rozdiel 1 minúta")
    void isValid_OneMinuteDifference_ReturnsTrue() {
        Event event = new Event();
        event.setStartTime(LocalDateTime.of(2024, 12, 20, 10, 0));
        event.setEndTime(LocalDateTime.of(2024, 12, 20, 10, 1));

        boolean isValid = validator.isValid(event, context);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validácia úspešná - endTime o sekundu neskôr")
    void isValid_OneSecondDifference_ReturnsTrue() {
        Event event = new Event();
        event.setStartTime(LocalDateTime.of(2024, 12, 20, 10, 0, 0));
        event.setEndTime(LocalDateTime.of(2024, 12, 20, 10, 0, 1));

        boolean isValid = validator.isValid(event, context);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validácia - inicializácia validátora")
    void initialize_DoesNotThrow() {
        EndTimeAfterStartTime annotation = null;

        assertDoesNotThrow(() -> validator.initialize(annotation));
    }
}
