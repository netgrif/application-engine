package com.netgrif.application.engine.validation.validator.text;

import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TelNumberValidation implements IValidator<TextField> {

    public static String telNumberRegex = "^(?:\\+?(\\d{1,3}))?([-. (]*(\\d{3})[-. )]*)?((\\d{3})[-. ]*(\\d{2,4})(?:[-.x ]*(\\d+))?)$";

    @Override
    public void validate(TextField field, DataField dataField) throws ValidationException {
        Optional<Validation> possibleValidation = getPossibleValidation(field);
        if (possibleValidation.isEmpty()) {
            return;
        }
        Validation validation = possibleValidation.get();
        String value = (String) dataField.getValue();
        if (value == null || value.length() == 0) {
            return;
        }
        if (!value.matches(telNumberRegex)) {
            throwValidationException(validation, "Invalid value of field [" + field.getImportId() + "], value [" + value + "] is not a valid phone number");
        }
    }

    public String getName() {
        return "telnumber";
    }
}