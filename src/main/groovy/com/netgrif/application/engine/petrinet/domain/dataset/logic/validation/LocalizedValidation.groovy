package com.netgrif.application.engine.petrinet.domain.dataset.logic.validation

class LocalizedValidation implements Serializable {

    static final long serialVersionUID = 412530951556364618L

    private String validationRule

    private String validationMessage

    LocalizedValidation(String validationRule, String validationMessage) {
        this.validationRule = validationRule
        this.validationMessage = validationMessage
    }

    String getValidationMessage() {
        return validationMessage
    }

    void setValidationMessage(String valMessage) {
        this.validationMessage = valMessage
    }

    String getValidationRule() {
        return validationRule
    }

    void setValidationRule(String validationRule) {
        this.validationRule = validationRule
    }
}
