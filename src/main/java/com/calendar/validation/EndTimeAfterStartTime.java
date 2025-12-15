package com.calendar.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Vlastná validačná anotácia pre kontrolu časov udalostí.
 *
 * <p>Zabezpečuje, že čas ukončenia (endTime) udalosti je neskôr
 * ako čas začiatku (startTime). Aplikuje sa na úrovni triedy Event.</p>
 *
 * <p>Validáciu vykonáva {@link EndTimeAfterStartTimeValidator}.</p>
 *
 * <p>Príklad použitia:
 * <pre>
 * {@code
 * @EndTimeAfterStartTime
 * public class Event {
 *     private LocalDateTime startTime;
 *     private LocalDateTime endTime;
 *     // ...
 * }
 * }
 * </pre></p>
 *
 * @see EndTimeAfterStartTimeValidator
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EndTimeAfterStartTimeValidator.class)
@Documented
public @interface EndTimeAfterStartTime {

    /**
     * Chybová správa zobrazená pri neúspešnej validácii.
     * @return predvolená chybová správa
     */
    String message() default "End time must be after start time";

    /**
     * Umožňuje špecifikovať validačné skupiny.
     * @return pole validačných skupín
     */
    Class<?>[] groups() default {};

    /**
     * Môže byť použité klientmi anotácie na priradenie vlastných objektov.
     * @return pole payload tried
     */
    Class<? extends Payload>[] payload() default {};
}