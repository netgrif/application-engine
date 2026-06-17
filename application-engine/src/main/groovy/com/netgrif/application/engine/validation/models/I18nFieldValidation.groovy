package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.validation.domain.ValidationDataInput

class I18nFieldValidation extends AbstractFieldValidation {

//    TRANSLATION_REQUIRED = 'translationRequired',
//    TRANSLATION_ONLY = 'translationOnly',
//    REQUIRED_I18N = 'requiredI18n'

    void wrongValue(ValidationDataInput validationData) {
        if (!(validationData.getData().getValue() == true)) {
            throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
        }
    }

}
