package com.netgrif.application.engine.petrinet.domain.dataset.logic.validation


class LocalizedValidation {

    protected String name

    private Map<String, ValidationRule> validationRule

    private String validationMessage

    LocalizedValidation(String  name, Map<String, ValidationRule> validationRule, String validationMessage) {
        this.name = name
        this.validationRule = validationRule
        this.validationMessage = validationMessage
    }

    String getValidationMessage() {
        return validationMessage
    }

    void setValidationMessage(String valMessage) {
        this.validationMessage = valMessage
    }

    Map<String, ValidationRule> getValidationRule() {
        return validationRule
    }

    void setValidationRule(Map<String, ValidationRule> validationRule) {
        this.validationRule = validationRule
    }
}
