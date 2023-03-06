package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.validation.domain.ValidationDataInput

class AbstractFieldValidation {

    void notempty(ValidationDataInput validationData) {
        if (validationData.getData().getValue() == null || validationData.getData().getRawValue() == null) {
            throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
        }
    }
}
