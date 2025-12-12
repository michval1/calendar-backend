package com.calendar.validation;

import com.calendar.model.Event;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EndTimeAfterStartTimeValidator implements ConstraintValidator<EndTimeAfterStartTime, Event> {

    @Override
    public void initialize(EndTimeAfterStartTime constraintAnnotation) {
    }

    @Override
    public boolean isValid(Event event, ConstraintValidatorContext context) {
        if (event == null || event.getStartTime() == null || event.getEndTime() == null) {
            return true; // nech to skontrolujú iné validácie
        }

        return event.getEndTime().isAfter(event.getStartTime());
    }
}