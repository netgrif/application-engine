package com.netgrif.application.engine.validation.validator.number;

import com.netgrif.application.engine.petrinet.domain.dataset.NumberField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PositiveValidation implements IValidator<NumberField> {
    @Override
    public void validate(NumberField field, DataField dataField) throws ValidationException {
        Optional<Validation> possibleValidation = getPossibleValidation(field);
        if (possibleValidation.isEmpty()) {
            return;
        }
        Validation validation = possibleValidation.get();
        Double value = (Double) dataField.getValue();
        if (value == null || value.equals(0D)) {
            return;
        }
        if (value < 0) {
            throwValidationException(validation, "Invalid value of field [" + field.getImportId() + "], value [" + value + "] is negative, but should be positive.");
        }
    }

    public String getName() {
        return "positive";
    }
}