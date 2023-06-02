package com.netgrif.application.engine.validation.validator.bool;

import com.netgrif.application.engine.petrinet.domain.dataset.BooleanField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequiredTrueValidation implements IValidator<BooleanField> {

    @Override
    public void validate(BooleanField field, DataField dataField) throws ValidationException {
        Optional<Validation> possibleValidation = field.getValidations().stream().filter(v -> v.getName().equals(getName())).findFirst();
        if (possibleValidation.isEmpty()) {
            return;
        }
        Boolean value = (Boolean) dataField.getValue();
        if (value == null) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value is NULL");
        }

        if (!value) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] should set to true");
        }
    }

    public String getName() {
        return "requiredTrue";
    }
}