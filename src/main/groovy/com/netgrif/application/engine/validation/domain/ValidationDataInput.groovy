package com.netgrif.application.engine.validation.domain

import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.workflow.domain.DataField

public class ValidationDataInput {

    private DataField data

    private I18nString validationMessage

    private Locale locale

    private String validationRegex

    DataField getData() {
        return data
    }

    void setData(DataField data) {
        this.data = data
    }

    I18nString getValidationMessage() {
        return validationMessage
    }

    void setValidationMessage(I18nString validationMessage) {
        this.validationMessage = validationMessage
    }

    Locale getLocale() {
        return locale
    }

    void setLocale(Locale locale) {
        this.locale = locale
    }

    String getValidationRegex() {
        return validationRegex
    }

    void setValidationRegex(String validationRegex) {
        this.validationRegex = validationRegex
    }

    ValidationDataInput(DataField data, I18nString validationMessage, Locale locale, String validationRegex) {
        this.data = data
        this.validationMessage = validationMessage
        this.locale = locale
        this.validationRegex = validationRegex
    }
}
