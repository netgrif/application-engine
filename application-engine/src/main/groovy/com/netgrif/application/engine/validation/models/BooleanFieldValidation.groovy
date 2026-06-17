package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.validation.domain.ValidationDataInput

class BooleanFieldValidation extends AbstractFieldValidation {

//    REQUIRED_TRUE = 'requiredTrue'
//    REQUIRED = 'required'

    void requiredtrue(ValidationDataInput validationData) {
        if (!(validationData.getData().getValue() == true)) {
            throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
        }
    }


}
