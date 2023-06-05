package com.netgrif.application.engine.validation.validator;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.ParseException;
import java.util.Optional;

public interface IValidator<T extends Field<?>> {

    default Optional<Validation> getPossibleValidation(T field) {
        return field.getValidations().stream().filter(v -> v.getName().equals(getName())).findFirst();
    }

    default void throwValidationException(Validation validation, String defaultMessage) throws ValidationException {
        try {
            throw new ValidationException(validation.getTranslatedValidationMessage(LocaleContextHolder.getLocale()));
        } catch (NullPointerException e) {
            throw new ValidationException(defaultMessage);
        }
    }

    default void checkNull(Validation validation, T field, Object value) throws ValidationException {
        if (value == null) {
            throwValidationException(validation, "Invalid value of field [" + field.getImportId() + "], value is NULL");
        }
    }

    void validate(T field, DataField dataField) throws ValidationException, ParseException;
    String getName();
}
