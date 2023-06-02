package com.netgrif.application.engine.validation.validator.i18n;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.I18nField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class TranslationRequiredValidation implements IValidator<I18nField> {

    @Override
    public void validate(I18nField field, DataField dataField) throws ValidationException {
        Optional<Validation> possibleValidation = field.getValidations().stream().filter(v -> v.getName().equals(getName())).findFirst();
        if (possibleValidation.isEmpty()) {
            return;
        }
        Validation validation = possibleValidation.get();
        String languagesString = validation.getArguments().get("languages").getValue();
        String[] languages = languagesString.split(",");
        I18nString value = (I18nString) dataField.getValue();
        if (value == null) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value is NULL");
        }
        if (!Arrays.stream(languages).allMatch(lang -> value.getTranslations().containsKey(lang))) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] does not have all of required translations [" + languagesString + "]");
        }
    }

    public String getName() {
        return "translationRequired";
    }
}
