package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.Expression

abstract class ChoiceField<T> extends Field<T> {

    protected Set<I18nString> choices
    protected Expression choicesExpression

    ChoiceField() {
        super()
        choices = new LinkedHashSet<I18nString>()
    }

    ChoiceField(List<I18nString> values) {
        this()
        if (values != null)
            this.choices.addAll(values)
    }

    ChoiceField(Expression expression) {
        this()
        this.choicesExpression = expression
    }

    Set<I18nString> getChoices() {
        return choices
    }

    void setChoices(Set<I18nString> choices) {
        this.choices = choices
    }

    Expression getExpression() {
        return choicesExpression
    }

    void setExpression(Expression expression) {
        this.choicesExpression = expression
    }

    void setChoicesFromStrings(Collection<String> choices) {
        this.choices = new LinkedHashSet<>()
        choices.each {
            this.choices.add(new I18nString(it))
        }
    }

    boolean isDynamic() {
        return this.choicesExpression != null
    }
}