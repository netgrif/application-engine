package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.validation;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.runner.Expression;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
public class DynamicValidation extends Validation {

    private String compiledRule;

    private Expression expression;

    public DynamicValidation(String validationRule) {
        this(validationRule, null);
    }

    public DynamicValidation(String validationRule, I18nString validationMessage) {
        super(validationRule, validationMessage);
        this.expression = new Expression("\"" + validationRule + "\"");
    }

    public LocalizedValidation getLocalizedValidation(Locale locale) {
        return new LocalizedValidation(this.compiledRule, getTranslatedValidationMessage(locale));
    }

    @Override
    public Validation clone() {
        return new DynamicValidation(this.getValidationRule(), this.getValidationMessage());
    }
}
