package com.netgrif.application.engine.petrinet.domain.validation;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.Data;

import java.util.List;
import java.util.Locale;

@Data
public class Validation {

    protected String name;

    protected List validationRule;

    protected I18nString validationMessage;

    Validation(List validationRule) {
        this(validationRule, null);
    }

    Validation(List validationRule, I18nString validationMessage) {
        this();
        this.validationRule = validationRule;
        this.validationMessage = validationMessage;
    }

    Validation() {}

//    LocalizedValidation getLocalizedValidation(Locale locale) {
//        LocalizedValidation ret = new LocalizedValidation(this.validationRule, getTranslatedValidationMessage(locale))
//        return ret;
//    }

    I18nString getValidationMessage() {
        return validationMessage;
    }

    void setValidationMessage(I18nString valMessage) {
        this.validationMessage = valMessage;
    }

    String getTranslatedValidationMessage(Locale locale) {
        return validationMessage

                .getTranslation(locale);
    }

    List getValidationRule() {
        return validationRule;
    }

    void setValidationRule(List validationRule) {
        this.validationRule = validationRule;
    }

}
