package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import lombok.Data;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
public abstract class ChoiceField<T> extends Field<T> {

    protected Set<I18nString> choices;
    protected Expression choicesExpression;

    public ChoiceField() {
        super();
    }

    public ChoiceField(List<I18nString> choices) {
        this();
        if (choices != null) {
            if (this.choices == null) {
                this.choices = new LinkedHashSet<>();
            }
            this.choices.addAll(choices);
        }
    }

    public ChoiceField(Expression expression) {
        this();
        this.choicesExpression = expression;
    }

    public Expression getExpression() {
        return choicesExpression;
    }

    public void setExpression(Expression choicesExpression) {
        this.choicesExpression = choicesExpression;
    }

    public void setChoicesFromStrings(Collection<String> choices) {
        this.choices = new LinkedHashSet<>();
        choices.forEach(choice -> this.choices.add(new I18nString(choice)));
    }

    public boolean isDynamic() {
        return this.choicesExpression != null;
    }
}