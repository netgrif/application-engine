package com.netgrif.application.engine.validation.models.text;

import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.ValidationRule;
import com.netgrif.application.engine.validation.service.interfaces.Validation;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RegexValidation implements Validation<TextField> {

    public void validate(TextField field) throws Exception {
        com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation validation = field.getValidations().get(getName());

        ValidationRule validationRule = validation.getValidationRule().get("expression");

        String patternString = null;

        String value = field.getRawValue();

        if (value == null) {
            return; //TODO: JOZI chyba ??
        }

        if (patternString == null) {
            return;
        }

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(validation.getValidationMessage().getTranslation(LocaleContextHolder.getLocale()));
        }

    }

    public String getName() {
        return "regex";
    }
}