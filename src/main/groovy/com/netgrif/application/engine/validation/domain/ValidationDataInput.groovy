package com.netgrif.application.engine.validation.domain

import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import lombok.AllArgsConstructor
import lombok.Data

class ValidationDataInput {

    Field<?> data

    I18nString validationMessage

    Locale locale

    String validationRegex

    ValidationDataInput(Field<?> data, I18nString validationMessage, Locale locale, String validationRegex) {
        this.data = data
        this.validationMessage = validationMessage
        this.locale = locale
        this.validationRegex = validationRegex
    }
}
