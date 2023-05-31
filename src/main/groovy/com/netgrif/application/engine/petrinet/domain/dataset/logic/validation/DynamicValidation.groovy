package com.netgrif.application.engine.petrinet.domain.dataset.logic.validation

import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.Expression
import org.springframework.data.annotation.Transient
import java.util.List

class DynamicValidation extends Validation {

    @Transient
    private List compiledRule

    private Expression expression

    DynamicValidation(String name, List validationRule) {
        this(name, validationRule, null)
    }

    DynamicValidation(String name, List validationRule, I18nString validationMessage) {
        super(name, validationRule, validationMessage)
        this.expression = new Expression("\"$validationRule\"" as String)
    }

    DynamicValidation() {}

    List getCompiledRule() {
        return compiledRule
    }

    void setCompiledRule(List compiledRule) {
        this.compiledRule = compiledRule
    }

    Expression getExpression() {
        return expression
    }

    LocalizedValidation getLocalizedValidation(Locale locale) {
        LocalizedValidation ret = new LocalizedValidation(this.name, this.compiledRule, getTranslatedValidationMessage(locale))
        return ret
    }

    @Override
    Validation clone() {
        return new DynamicValidation(this.name, this.validationRule, this.validationMessage)
    }
}
