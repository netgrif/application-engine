package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.Expression

abstract class MapOptionsField<T, U> extends Field<U> {

    protected Map<String, T> options
    protected Expression optionsExpression

    MapOptionsField() {
        this(new HashMap<String, T>())
    }

    MapOptionsField(Map<String, T> options) {
        super()
        this.options = options
    }

    MapOptionsField(Expression expression) {
        super()
        this.optionsExpression = expression
    }

    Map<String, T> getOptions() {
        return options
    }

    void setOptions(Map<String, T> options) {
        this.options = options
    }

    Expression getExpression() {
        return optionsExpression
    }

    void setExpression(Expression expression) {
        this.optionsExpression = expression
    }

    boolean isDynamic() {
        return this.optionsExpression != null
    }
}
