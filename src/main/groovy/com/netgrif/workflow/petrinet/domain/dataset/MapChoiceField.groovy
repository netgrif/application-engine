package com.netgrif.workflow.petrinet.domain.dataset

abstract class MapChoiceField<T> extends FieldWithDefault<String> {

    protected Map<String, T> choices

    MapChoiceField() {
        this(new HashMap<String, T>())
    }

    MapChoiceField(Map<String, T> choices) {
        super()
        this.choices = choices
    }

    Map<String, T> getChoices() {
        return choices
    }

    void setChoices(Map<String, T> choices) {
        this.choices = choices
    }
}
