package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.runner.Expression;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class ChoiceField<T> extends Field<T> {

    public ChoiceField() {
        super();
        choices = new LinkedHashSet<>();
    }

    public ChoiceField(List<I18nString> values) {
        this();
        if (values != null) this.choices.addAll(values);
    }

    public ChoiceField(Expression expression) {
        this();
        this.choicesExpression = expression;
    }

    public Expression getExpression() {
        return choicesExpression;
    }

    public void setExpression(Expression expression) {
        this.choicesExpression = expression;
    }

    public void setChoicesFromStrings(Collection<String> choices) {
        this.choices = new LinkedHashSet<>();
        choices.forEach(it -> this.getChoices().add(new I18nString(it)));
    }

    public boolean isDynamic() {
        return this.choicesExpression != null;
    }

    @Getter
    @Setter
    protected Set<I18nString> choices;
    protected Expression choicesExpression;
}
