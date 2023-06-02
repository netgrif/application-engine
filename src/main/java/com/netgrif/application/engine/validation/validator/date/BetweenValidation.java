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
        Optional<Validation> possibleValidation = field.getValidations().stream().filter(v -> v.getName().equals(getName())).findFirst();
        if (possibleValidation.isEmpty()) {
            return;
        }
        Validation validation = possibleValidation.get();
        Date fromDate = null, toDate = null;

        String from = validation.getArguments().get("from").getValue();
        if (from.equals(TODAY)) {
            fromDate = new Date();
        } else if (!List.of(PAST, FUTURE).contains(from)) {
            fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(from);
        }


        String to = validation.getArguments().get("to").getValue();
        if (to.equals(TODAY)) {
            toDate = new Date();
        } else if (!List.of(PAST, FUTURE).contains(to)) {
            toDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(to);
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

        if (from.equals(PAST) && value.after(toDate)) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be in range [" + from + ", " + to + "]" );
        }

        if (value.before(fromDate) && to.equals(FUTURE)) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be in range [" + from + ", " + to + "]" );
        }

        if (!(value.after(fromDate) && value.before(toDate))) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be in range [" + from + ", " + to + "]" );
        }
    }

    public String getName() {
        return "between";
    }
}