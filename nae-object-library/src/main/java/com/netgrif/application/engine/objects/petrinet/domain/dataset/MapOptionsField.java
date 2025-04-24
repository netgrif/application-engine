package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.runner.Expression;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public abstract class MapOptionsField<T, U> extends Field<U> {

    @Getter
    @Setter
    protected Map<String, T> options;
    protected Expression optionsExpression;

    public MapOptionsField() {
        this(new HashMap<String, T>());
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

    public void setExpression(Expression expression) {
        this.optionsExpression = expression;
    }

    public boolean isDynamic() {
        return this.optionsExpression != null;
    }
}
