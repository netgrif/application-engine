package com.netgrif.application.engine.petrinet.domain.dataset.logic.validation

import com.netgrif.application.engine.petrinet.domain.I18nString
import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType

class Validation {

    protected String name

    protected String validationRule

    protected Map<String, Argument> arguments

    protected I18nString validationMessage

    Validation(String validationRule) {
        this(validationRule, null as I18nString)
    }

    Validation(String validationRule, I18nString validationMessage) {
        this.validationRule = validationRule
        this.validationMessage = validationMessage
    }

    Validation(String name, Map<String, Argument> arguments, I18nString validationMessage) {
        this()
        this.name = name
        this.arguments = arguments
        this.validationMessage = validationMessage
    }

    Validation(String name, String validationRule, Map<String, Argument> arguments, I18nString validationMessage) {
        this.name = name
        this.validationRule = validationRule
        this.arguments = arguments
        this.validationMessage = validationMessage
    }

    Validation() {}

    LocalizedValidation getLocalizedValidation(Locale locale) {
        LocalizedValidation ret = new LocalizedValidation(name, this.arguments, getTranslatedValidationMessage(locale))
        return ret
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
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

    Map<String, Argument> getArguments() {
        return arguments
    }

    void setArguments(Map<String, Argument> arguments) {
        this.arguments = arguments
    }

    String getValidationRule() {
        return validationRule
    }

    void setValidationRule(String validationRule) {
        this.validationRule = validationRule
    }

    @Override
    Validation clone() {
        return new Validation(this.name, this.validationRule, this.arguments, this.validationMessage)
    }

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }
}
