package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.validation.domain.ValidationDataInput

class NumberFieldValidation extends AbstractFieldValidation {
    static final String INF = 'inf'

//    static final String ODD = 'odd'
//    static final String EVEN = 'even'
//    static final String POSITIVE = 'positive'
//    static final String NEGATIVE = 'negative'
//    static final String DECIMAL = 'decimal'
//    static final String IN_RANGE = 'inrange'
//    static final String INF = 'inf'
//    static final String REQUIRED = 'required'
//    static final String VALID_ODD = 'validOdd'
//    static final String VALID_EVEN = 'validEven'
//    static final String VALID_POSITIVE = 'validPositive'
//    static final String VALID_NEGATIVE = 'validNegative'
//    static final String VALID_DECIMAL = 'validDecimal'
//    static final String VALID_IN_RANGE = 'validInRange'


    void odd(ValidationDataInput validationData) {
        if (!(validationData.getData().getValue() == 0 || validationData.getData().getValue() == null)) {
            if (validationData.getData().getValue() % 2 == 0) {
                throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
            }
        }
    }

    void even(ValidationDataInput validationData) {
        if (!(validationData.getData().getValue() == 0 || validationData.getData().getValue() == null)) {
            if (validationData.getData().getValue() % 2 != 0) {
                throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
            }
        }
    }

    void positive(ValidationDataInput validationData) {
        if (!(validationData.getData().getValue() == 0 || validationData.getData().getValue() == null)) {
            if (validationData.getData().getValue() < 0) {
                throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
            }
        }
    }

    void negative(ValidationDataInput validationData) {
        if (!(validationData.getData().getValue() == 0 || validationData.getData().getValue() == null)) {


            if (validationData.getData() != null) {
                if (validationData.getData().getValue() > 0) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            }
        }
    }

    void decimal(ValidationDataInput validationData) {
        if (!(validationData.getData().getValue() == 0 || validationData.getData().getValue() == null)) {

            if (validationData.getData().getValue() % 1 != 0) {
                throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
            }
        }
    }

    void inrange(ValidationDataInput validationData) {
        if (!(validationData.getData().getValue() == 0 || validationData.getData().getValue() == null)) {

            String[] ranges = validationData.getValidationRegex().split(",")
            if (ranges.size() == 2) {
                if (ranges.any { it.toLowerCase().equals(INF) }) {
                    if (ranges.first().toLowerCase() != INF) {
                        if (validationData.getData().getValue() < ranges[0] as Double) {
                            throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                        }
                    } else {
                        if (ranges[1].toLowerCase() != INF) {
                            if (validationData.getData().getValue() > ranges[1] as Double) {
                                throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                            }
                        }
                    }
                } else {
                    if (validationData.getData().getValue() < ranges[0] as Double || validationData.getData().getValue() > ranges[1] as Double) {
                        throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                    }
                }
            }
        }
    }

}
