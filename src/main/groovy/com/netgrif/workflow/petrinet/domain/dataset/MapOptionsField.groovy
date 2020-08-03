package com.netgrif.workflow.petrinet.domain.dataset

abstract class MapOptionsField<T, U> extends FieldWithDefault<U> {

    protected Map<String, T> options

    MapOptionsField() {
        this(new HashMap<String, T>())
    }

    MapOptionsField(Map<String, T> options) {
        super()
        this.options = options
    }

    Map<String, T> getOptions() {
        return options
    }

    void setOptions(Map<String, T> choices) {
        this.options = choices
    }
}
