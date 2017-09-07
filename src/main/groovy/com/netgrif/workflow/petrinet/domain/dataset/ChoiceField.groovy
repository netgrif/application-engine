package com.netgrif.workflow.petrinet.domain.dataset

abstract class ChoiceField<T> extends FieldWithDefault<T> {

    private Set<String> choices

    ChoiceField() {
        super()
        choices = new LinkedHashSet<>()
    }

    ChoiceField(String[] values) {
        this()
        if (values != null)
            this.choices.addAll(values)
    }

    Set<String> getChoices() {
        return choices
    }

    void setChoices(Set<String> choices) {
        this.choices = choices
    }
}
