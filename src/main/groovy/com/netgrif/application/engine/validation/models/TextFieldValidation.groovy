package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.validation.domain.ValidationDataInput

class TextFieldValidation extends AbstractFieldValidation {

    public static String telNumberRegex = '^(?:\\+?(\\d{1,3}))?([-. (]*(\\d{3})[-. )]*)?((\\d{3})[-. ]*(\\d{2,4})(?:[-.x ]*(\\d+))?)$'
    public static String emailRegex = '^[a-zA-Z0-9\\._\\%\\+\\-]+@[a-zA-Z0-9\\.\\-]+\\.[a-zA-Z]{2,}$'
//    REQUIRED = 'required'
//    MIN_LENGTH = 'minLength'
//    MAX_LENGTH = 'maxLength'
//    VALID_MIN_LENGTH = 'minlength'
//    VALID_MAX_LENGTH = 'maxlength'
//    PATTERN = 'pattern'
//    REGEX = 'regex'
//    VALID_TEL_NUMBER = 'validTelNumber'
//    TEL_NUMBER = 'telNumber'
//    EMAIL = 'email'

    void regex(ValidationDataInput validationData) {
        if (validationData.getData().getValue() != null && validationData.getData().getRawValue() != null) {
            if (!(validationData.getData().getRawValue() ==~ validationData.getValidationRegex())) {
                throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
            }
        }
    }

    void minlength(ValidationDataInput validationData) {
        if (validationData.getData().getValue() != null && validationData.getData().getRawValue() != null) {
            if (!((validationData.getData().getRawValue() as String).length() >= (validationData.getValidationRegex() as Integer))) {
                throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
            }
        }
    }

    void maxlength(ValidationDataInput validationData) {
        if (validationData.getData().getValue() != null && validationData.getData().getRawValue() != null) {
            if ((validationData.getData().getRawValue() as String).length() > (validationData.getValidationRegex() as Integer)) {
                throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
            }
        }
    }

    void telnumber(ValidationDataInput validationData) {
        if (validationData.getData().getValue() != null && validationData.getData().getRawValue() != null) {
            if (!(validationData.getData().getRawValue() ==~ telNumberRegex)) {
                throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
            }
        }
    }

    void email(ValidationDataInput validationData) {
        if (validationData.getData().getValue() != null && validationData.getData().getRawValue() != null) {
            if (!(validationData.getData().getRawValue() ==~ emailRegex)) {
                throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
            }
        }
    }

}
