package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.Expression;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public abstract class MapOptionsField<T, U> extends Field<U> {

    protected Map<String, T> options;
    protected Expression optionsExpression;

    public MapOptionsField() {
        this(new HashMap<>());
    }

    public MapOptionsField(Map<String, T> options) {
        super();
        this.options = options;
    }

    public MapOptionsField(Expression expression) {
        super();
        this.optionsExpression = expression;
    }

    public Expression getExpression() {
        return optionsExpression;
    }

    public void setExpression(Expression choicesExpression) {
        this.optionsExpression = choicesExpression;
    }

    public boolean isDynamic() {
        return this.optionsExpression != null;
    }
}