package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.petrinet.domain.dataset.BooleanField
import com.netgrif.application.engine.validation.domain.ValidationDataInput

class BooleanFieldValidation extends AbstractFieldValidation {

//    REQUIRED_TRUE = 'requiredTrue'
//    REQUIRED = 'required'

    void requiredtrue(ValidationDataInput validationData) {
        Boolean value = ((BooleanField) validationData.getData()).getRawValue()
        if (!(value == true) || value == null) {
            throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
        }
    }
}
