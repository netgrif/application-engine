package com.netgrif.application.engine.validation.validator.date;

import com.netgrif.application.engine.importer.service.FieldFactory;
import com.netgrif.application.engine.petrinet.domain.dataset.DateField;
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.utils.DateUtils;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class BetweenValidation implements IValidator<Field<?>> {

    static final String PAST = "past";
    static final String FUTURE = "future";
    static final String TODAY = "today";

    @Override
    public void validate(Field<?> field, DataField dataField) throws ValidationException, ParseException {
        Optional<Validation> possibleValidation = getPossibleValidation(field);
        if (possibleValidation.isEmpty()) {
            return;
        }
        Validation validation = possibleValidation.get();
        String from = validation.getArguments().get("from").getValue();
        String to = validation.getArguments().get("to").getValue();
        Date fromDate = parseFromString(field, from);
        Date toDate = parseFromString(field, to);

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
        if (from.equals(PAST)) {
            if (value.after(toDate)) {
                throwValidationException(validation, "Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be in range [" + from + ", " + to + "]");
            }
        } else if (to.equals(FUTURE)) {
            if (value.before(fromDate)) {
                throwValidationException(validation, "Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be in range [" + from + ", " + to + "]");
            }
        } else if (value.before(fromDate) || value.after(toDate)) {
            throwValidationException(validation, "Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be in range [" + from + ", " + to + "]" );
        }
    }

    public String getName() {
        return "between";
    }

    private Date parseFromString(Field<?> field, String dateString) {
        Date resultDate = null;
        if (dateString.equals(TODAY)) {
            resultDate = new Date();
        } else if (!List.of(PAST, FUTURE).contains(dateString)) {
            resultDate = field.getType().equals(FieldType.DATE) ? DateUtils.localDateToDate(FieldFactory.parseDateFromString(dateString)) : DateUtils.localDateTimeToDate(FieldFactory.parseDateTimeFromString(dateString));
        }
        return resultDate;
    }
}