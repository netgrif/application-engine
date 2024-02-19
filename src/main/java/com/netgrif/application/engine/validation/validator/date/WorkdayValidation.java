package com.netgrif.application.engine.validation.validator.date;

import com.netgrif.application.engine.petrinet.domain.dataset.DateField;
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.utils.DateUtils;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.*;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Component
public class WorkdayValidation implements IValidator<Field<?>> {
    @Override
    public void validate(Field<?> field, DataField dataField) throws ValidationException, ParseException {
        Optional<Validation> possibleValidation = getPossibleValidation(field);
        if (possibleValidation.isEmpty()) {
            return;
        }

        Validation validation = possibleValidation.get();
        Date value;
        if (dataField.getValue() == null) {
            value = null;
        } else if (field instanceof DateField) {
            value = dataField.getValue() instanceof LocalDate ? DateUtils.localDateToDate((LocalDate) dataField.getValue()) : (Date) dataField.getValue();
        } else if (field instanceof DateTimeField) {
            value = dataField.getValue() instanceof LocalDateTime ? DateUtils.localDateTimeToDate((LocalDateTime) dataField.getValue()) : (Date) dataField.getValue();
        } else {
            return;
        }
        if (value == null) {
            return;
        }
        if (isWeekend(value)) {
            throwValidationException(validation, "Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be day of workdays" );
        }
    }

    protected boolean isWeekend(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        DayOfWeek dayOfWeek = DayOfWeek.of(localDate.get(ChronoField.DAY_OF_WEEK));
        return dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY;
    }

    public String getName() {
        return "workday";
    }
}