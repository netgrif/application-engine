package com.netgrif.application.engine.validation.validator.bool;

import com.netgrif.application.engine.petrinet.domain.dataset.BooleanField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequiredTrueValidation implements IValidator<BooleanField> {

    @Override
    public void validate(BooleanField field, DataField dataField) throws ValidationException {
        Optional<Validation> possibleValidation = getPossibleValidation(field);
        if (possibleValidation.isEmpty()) {
            return;
        }
        Validation validation = possibleValidation.get();
        Boolean value = (Boolean) dataField.getValue();
        if (value == null) {
            return;
        }
        if (!value) {
            throwValidationException(validation, "Invalid value of field [" + field.getImportId() + "], value [" + value + "] should set to true");
        }
    }

    public String getName() {
        return "requiredTrue";
    }
}