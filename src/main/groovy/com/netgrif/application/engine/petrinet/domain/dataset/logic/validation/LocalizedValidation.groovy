package com.netgrif.application.engine.petrinet.domain.dataset.logic.validation


class LocalizedValidation {

    private String name

    private Map<String, Argument> arguments

    private String validationMessage

    LocalizedValidation(String  name, Map<String, Argument> arguments, String validationMessage) {
        this.name = name
        this.arguments = arguments
        this.validationMessage = validationMessage
    }

    String getValidationMessage() {
        return validationMessage
    }

    void setValidationMessage(String valMessage) {
        this.validationMessage = valMessage
    }

    Map<String, Argument> getArguments() {
        return arguments
    }

    void setArguments(Map<String, Argument> arguments) {
        this.arguments = arguments
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }
}
