package com.netgrif.application.engine.validation.validator.text;

import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MaxLengthValidation implements IValidator<TextField> {
    @Override
    public void validate(TextField field, DataField dataField) throws ValidationException {
        Optional<Validation> possibleValidation = field.getValidations().stream().filter(v -> v.getName().equals(getName())).findFirst();
        if (possibleValidation.isEmpty()) {
            return;
        }
        Validation validation = possibleValidation.get();
        double maxLength = Double.parseDouble(validation.getArguments().get("max").getValue());
        String value = (String) dataField.getValue();
        if (value == null) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value is NULL");
        }
        if (value.length() > maxLength) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] is longer than [" + maxLength + "] characters.");
        }
    }

    public String getName() {
        return "maxLength";
    }
}