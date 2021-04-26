package com.netgrif.workflow.petrinet.domain.dataset

abstract class MapOptionsField<T, U> extends Field<U> {

    protected Map<String, T> options
    protected String optionsExpression

    MapOptionsField() {
        this(new HashMap<String, T>())
    }

    MapOptionsField(Map<String, T> options) {
        super()
        this.options = options
    }

    MapOptionsField(String expression) {
        super()
        this.optionsExpression = expression
    }

    Map<String, T> getOptions() {
        return options
    }

    void setOptions(Map<String, T> options) {
        this.options = options
    }

    String getExpression() {
        return optionsExpression
    }

    void setExpression(String expression) {
        this.optionsExpression = expression
    }

    boolean isDynamic() {
        return this.optionsExpression != null
    }
}
