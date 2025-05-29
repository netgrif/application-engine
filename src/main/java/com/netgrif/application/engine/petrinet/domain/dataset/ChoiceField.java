package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import lombok.Data;

import java.util.*;

@Data
// TODO: release/8.0.0 rename
public abstract class ChoiceField<T> extends Field<T> {

    protected LinkedHashSet<I18nString> choices;
    protected Expression<LinkedHashSet<I18nString>> choicesExpression;

    public ChoiceField() {
        super();
        choices = new LinkedHashSet<>();
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

    public ChoiceField(Expression<LinkedHashSet<I18nString>> expression) {
        this();
        this.choicesExpression = expression;
    }

    public Expression<LinkedHashSet<I18nString>> getExpression() {
        return choicesExpression;
    }

    public void setExpression(Expression<LinkedHashSet<I18nString>> choicesExpression) {
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