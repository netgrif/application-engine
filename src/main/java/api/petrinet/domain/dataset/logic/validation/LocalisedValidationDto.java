package api.petrinet.domain.dataset.logic.validation;

public class LocalisedValidationDto {

    private String validationRule;

    private String validationMessage;

    public String getValidationRule() {
        return validationRule;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
}
