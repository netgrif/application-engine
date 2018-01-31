package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.petrinet.domain.I18nString

abstract class ChoiceField<T> extends FieldWithDefault<T> {

    private Set<I18nString> choices

    ChoiceField() {
        super()
        choices = new LinkedHashSet<>()
    }

    ChoiceField(I18nString[] values) {
        this()
        if (values != null)
            this.choices.addAll(values)
    }

    Set<I18nString> getChoices() {
        return choices
    }

    void setChoices(Set<I18nString> choices) {
        this.choices = choices
    }
}
