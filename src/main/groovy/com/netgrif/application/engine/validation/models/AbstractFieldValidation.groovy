package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.validation.domain.ValidationDataInput

class AbstractFieldValidation {

    void notempty(ValidationDataInput validationData) {
        if (validationData.getData().getValue() == "" || validationData.getData().getValue() == []) {
            throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
        }
    }
}
