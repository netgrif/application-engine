package com.netgrif.application.engine.validation.validator.date;

import com.netgrif.application.engine.petrinet.domain.dataset.DateField;
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class WeekendValidation implements IValidator<Field<?>> {

    @Override
    public void validate(Field<?> field, DataField dataField) throws ValidationException, ParseException {
        Optional<Validation> possibleValidation = field.getValidations().stream().filter(v -> v.getName().equals(getName())).findFirst();
        if (possibleValidation.isEmpty()) {
            return;
        }

        Date value;
        if (dataField.getValue() == null) {
            value = null;
        } else if (field instanceof DateField) {
            value = Date.from(((LocalDate) dataField.getValue()).atStartOfDay().toInstant(ZoneOffset.of(ZoneId.systemDefault().getId())));
        } else if (field instanceof DateTimeField) {
            value = Date.from(((LocalDateTime) dataField.getValue()).toInstant(ZoneOffset.of(ZoneId.systemDefault().getId())));
        } else {
            return;
        }

        if (value == null) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value is NULL");
        }

        if (!(isWeekend(value))) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be day of weekend" );
        }
    }

    protected static boolean isWeekend(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DayOfWeek dayOfWeek = DayOfWeek.of(calendar.get(Calendar.DAY_OF_WEEK));
        return dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY;
    }

    public String getName() {
        return "weekend";
    }
}