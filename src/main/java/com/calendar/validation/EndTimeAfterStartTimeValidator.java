package com.calendar.validation;

import com.calendar.model.Event;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validátor pre vlastnú anotáciu {@link EndTimeAfterStartTime}.
 *
 * <p>Implementuje business logiku validácie, ktorá kontroluje, či je
 * čas ukončenia udalosti (endTime) neskôr ako čas začiatku (startTime).</p>
 *
 * <p>Validačné pravidlá:
 * <ul>
 *   <li>Ak sú startTime alebo endTime null, validácia prejde (tieto
 *       situácie ošetrujú iné validácie)</li>
 *   <li>endTime musí byť striktne neskôr ako startTime</li>
 *   <li>Rovnaký čas začiatku a ukončenia nie je povolený</li>
 * </ul></p>
 *
 * <p>Tento validátor automaticky zabezpečuje, že používatelia nemôžu
 * vytvoriť udalosť s nezmyselným časovým rozsahom.</p>
 *
 * @see EndTimeAfterStartTime
 */
public class EndTimeAfterStartTimeValidator implements ConstraintValidator<EndTimeAfterStartTime, Event> {

    /**
     * Inicializačná metóda validátora.
     * Volá sa automaticky pri vytváraní validátora.
     *
     * @param constraintAnnotation inštancia anotácie
     */
    @Override
    public void initialize(EndTimeAfterStartTime constraintAnnotation) {}

    /**
     * Vykoná validáciu objektu Event.
     *
     * <p>Kontroluje, či je endTime neskôr ako startTime.
     * Ak je event null alebo niektorý z časov je null, validácia
     * automaticky prejde - tieto prípady ošetrujú iné validácie
     * (@NotNull anotácie na jednotlivých poliach).</p>
     *
     * @param event udalosť na validáciu
     * @param context kontext validácie (umožňuje pridať vlastné chybové správy)
     * @return true ak je validácia úspešná, false ak zlyhá
     */
    @Override
    public boolean isValid(Event event, ConstraintValidatorContext context) {
        if (event == null || event.getStartTime() == null || event.getEndTime() == null) {
            return true;
        }
        return event.getEndTime().isAfter(event.getStartTime());
    }
}