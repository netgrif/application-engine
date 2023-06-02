package com.netgrif.application.engine.validation.validator.text;

import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RegexValidation implements IValidator<TextField> {

    @Override
    public void validate(TextField field, DataField dataField) throws ValidationException {
        Optional<Validation> possibleValidation = field.getValidations().stream().filter(v -> v.getName().equals(getName())).findFirst();
        if (possibleValidation.isEmpty()) {
            return;
        }
        Validation validation = possibleValidation.get();
        String patternString = validation.getArguments().get("expression").getValue();
        String value = (String) dataField.getValue();
        if (value == null) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value is NULL");
        }
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches()) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] does not match the pattern [" + patternString + "]");
        }
    }

    public String getName() {
        return "regex";
    }
}