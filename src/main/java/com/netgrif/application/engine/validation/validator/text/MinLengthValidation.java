package com.netgrif.application.engine.validation.validator.text;

import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MinLengthValidation implements IValidator<TextField> {

    @Override
    public void validate(TextField field, DataField dataField) throws ValidationException {
        Optional<Validation> possibleValidation = field.getValidations().stream().filter(v -> v.getName().equals(getName())).findFirst();
        if (possibleValidation.isEmpty()) {
            return;
        }
        Validation validation = possibleValidation.get();
        double minLength = Double.parseDouble(validation.getArguments().get("min").getValue());
        String value = (String) dataField.getValue();
        if (value == null) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value is NULL");
        }
        if (value.length() <= minLength) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] is shorter than [" + minLength + "] characters.");
        }
    }

    public String getName() {
        return "minLength";
    }
}