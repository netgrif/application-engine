package api.petrinet.domain.dataset.logic.validation;


import api.petrinet.domain.I18nStringDto;

public class ValidationDto {

    private String validationRule;

    private I18nStringDto validationMessage;

    public String getValidationRule() {
        return validationRule;
    }

    public I18nStringDto getValidationMessage() {
        return validationMessage;
    }

    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }

    public void setValidationMessage(I18nStringDto validationMessage) {
        this.validationMessage = validationMessage;
    }
}
