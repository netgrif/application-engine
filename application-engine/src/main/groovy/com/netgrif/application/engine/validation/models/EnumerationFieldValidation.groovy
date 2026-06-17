package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.validation.domain.ValidationDataInput

class EnumerationFieldValidation extends AbstractFieldValidation {

//    WRONG_VALUE = 'wrongValue',
//    REQUIRED = 'required'

    void wrongValue(ValidationDataInput validationData) {
        if (!(validationData.getData().getOptions().get(validationData.getData().getValue()))) {
            throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
        }
    }

}
