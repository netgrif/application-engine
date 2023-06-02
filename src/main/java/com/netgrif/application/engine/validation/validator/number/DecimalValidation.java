package com.netgrif.application.engine.validation.validator.number;

import com.netgrif.application.engine.petrinet.domain.dataset.NumberField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DecimalValidation implements IValidator<NumberField> {

    @Override
    public void validate(NumberField field, DataField dataField) throws ValidationException {
        Optional<Validation> possibleValidation = field.getValidations().stream().filter(v -> v.getName().equals(getName())).findFirst();
        if (possibleValidation.isEmpty()) {
            return;
        }
        Double value = (Double) dataField.getValue();
        if (value == null) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value is NULL");
        }
        if (value % 1 != 0) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] is not decimal.");
        }
    }

    public String getName() {
        return "decimal";
    }
}