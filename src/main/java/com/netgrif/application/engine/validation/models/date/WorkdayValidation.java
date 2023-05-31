package com.netgrif.application.engine.validation.models.date;

import com.netgrif.application.engine.petrinet.domain.dataset.DateField;
import com.netgrif.application.engine.validation.service.interfaces.Validation;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoField;

@Component
public class WorkdayValidation implements Validation<DateField> {
    public void validate(DateField field) throws Exception {
        com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation validation = field.getValidations().get(getName());

        if (validation == null) {
            throw new Exception("TOTOK");
        }
        LocalDate value = field.getRawValue();

        if (value == null) {
            throw new Exception("TOTOK");
        }

        if (isWeekend(value)) {
            throw new Exception("TOTOK");
        }

    }

    protected static boolean isWeekend(LocalDate day) {
        DayOfWeek dayOfWeek = DayOfWeek.of(day.get(ChronoField.DAY_OF_WEEK));
        return dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY;
    }

    public String getName() {
        return "workday";
    }
}