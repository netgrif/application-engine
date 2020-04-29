package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.I18nString
import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType

class Validation {

    private String validationRule

    private I18nString validationMessage

    Validation(String validationRule, I18nString validationMessage) {
        this.validationRule = validationRule
        this.validationMessage = validationMessage
    }

    Validation(String validationRule) {
        this.validationRule = validationRule
    }

    Validation() {
    }

    LocalizedValidation getLocalizedValidation(Locale locale){
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
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }
}
