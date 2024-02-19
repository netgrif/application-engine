package com.netgrif.application.engine.validation.validator.i18n;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.I18nField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class TranslationRequiredValidation implements IValidator<I18nField> {

    @Override
    public void validate(I18nField field, DataField dataField) throws ValidationException {
        Optional<Validation> possibleValidation = getPossibleValidation(field);
        if (possibleValidation.isEmpty()) {
            return;
        }
        Validation validation = possibleValidation.get();
        String languagesString = validation.getArguments().get("languages").getValue();
        String[] languages = languagesString.split(",");
        I18nString value = (I18nString) dataField.getValue();
        if (value == null) {
            return;
        }
        if (!Arrays.stream(languages).allMatch(lang -> value.getTranslations().containsKey(lang))) {
            throwValidationException(validation, "Invalid value of field [" + field.getImportId() + "], value [" + value + "] does not have all of required translations [" + languagesString + "]");
        }
    }

    public String getName() {
        return "translationRequired";
    }
}
