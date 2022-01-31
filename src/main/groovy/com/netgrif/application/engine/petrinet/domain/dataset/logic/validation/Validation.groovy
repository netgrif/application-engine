package com.netgrif.application.engine.petrinet.domain.dataset.logic.validation

import com.netgrif.application.engine.petrinet.domain.I18nString
import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType

class Validation {

    protected String validationRule

    protected I18nString validationMessage

    Validation(String validationRule) {
        this(validationRule, null)
    }

    Validation(String validationRule, I18nString validationMessage) {
        this()
        this.validationRule = validationRule
        this.validationMessage = validationMessage
    }

    Validation() {}

    LocalizedValidation getLocalizedValidation(Locale locale) {
        LocalizedValidation ret = new LocalizedValidation(this.validationRule, getTranslatedValidationMessage(locale))
        return ret
    }

    I18nString getValidationMessage() {
        return validationMessage
    }

    void setValidationMessage(I18nString valMessage) {
        this.validationMessage = valMessage
    }

    String getTranslatedValidationMessage(Locale locale) {
        return validationMessage?.getTranslation(locale)
    }

    String getValidationRule() {
        return validationRule
    }

    void setValidationRule(String validationRule) {
        this.validationRule = validationRule
    }

    @Override
    Validation clone() {
        return new Validation(this.validationRule, this.validationMessage)
    }

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }
}
