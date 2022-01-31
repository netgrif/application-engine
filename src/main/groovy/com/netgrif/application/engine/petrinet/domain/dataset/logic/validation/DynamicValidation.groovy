package com.netgrif.application.engine.petrinet.domain.dataset.logic.validation

import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.Expression
import org.springframework.data.annotation.Transient

class DynamicValidation extends Validation {

    @Transient
    private String compiledRule

    private Expression expression

    DynamicValidation(String validationRule) {
        this(validationRule, null)
    }

    DynamicValidation(String validationRule, I18nString validationMessage) {
        super(validationRule, validationMessage)
        this.expression = new Expression("\"$validationRule\"" as String)
    }

    DynamicValidation() {}

    String getCompiledRule() {
        return compiledRule
    }

    void setCompiledRule(String compiledRule) {
        this.compiledRule = compiledRule
    }

    Expression getExpression() {
        return expression
    }

    LocalizedValidation getLocalizedValidation(Locale locale) {
        LocalizedValidation ret = new LocalizedValidation(this.compiledRule, getTranslatedValidationMessage(locale))
        return ret
    }

    @Override
    Validation clone() {
        return new DynamicValidation(this.validationRule, this.validationMessage)
    }
}
