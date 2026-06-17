package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.validation;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

@Setter
@Getter
@NoArgsConstructor
public class Validation implements Serializable {

    @Serial
    private static final long serialVersionUID = 3287600522204188694L;

    protected String validationRule;

    protected I18nString validationMessage;

    public Validation(String validationRule) {
        this(validationRule, null);
    }

    public Validation(String validationRule, I18nString validationMessage) {
        this();
        this.validationRule = validationRule;
        this.validationMessage = validationMessage;
    }

    public LocalizedValidation getLocalizedValidation(Locale locale) {
        return new LocalizedValidation(this.validationRule, getTranslatedValidationMessage(locale));
    }

    public String getTranslatedValidationMessage(Locale locale) {
        if (this.validationMessage == null) {
            return null;
        }
        return validationMessage.getTranslation(locale);
    }

    @Override
    public Validation clone() {
        return new Validation(this.validationRule, this.validationMessage);
    }
}
