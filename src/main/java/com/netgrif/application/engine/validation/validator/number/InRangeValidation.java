package com.netgrif.application.engine.validation.validator.number;

import com.netgrif.application.engine.petrinet.domain.dataset.NumberField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InRangeValidation implements IValidator<NumberField> {

    static final String INF = "inf";

    @Override
    public void validate(NumberField field, DataField dataField) throws ValidationException {
        Optional<Validation> possibleValidation = getPossibleValidation(field);
        if (possibleValidation.isEmpty()) {
            return;
        }
        Validation validation = possibleValidation.get();
        String from = validation.getArguments().get("from").getValue();
        String to = validation.getArguments().get("to").getValue();
        Double value = (Double) dataField.getValue();
        if (value == null || value.equals(0D)) {
            return;
        }

        if (from.equals(INF) && value > Double.parseDouble(to)) {
            throwValidationException(validation, "Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be in range [" + from + ", " + to + "]" );
        }

        if (value < Double.parseDouble(from) && to.equals(INF)) {
            throwValidationException(validation, "Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be in range [" + from + ", " + to + "]" );
        }

        if (!(Double.parseDouble(from) <= value && value <= Double.parseDouble(to))) {
            throwValidationException(validation, "Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be in range [" + from + ", " + to + "]" );
        }
    }

    public String getName() {
        return "inrange";
    }
}