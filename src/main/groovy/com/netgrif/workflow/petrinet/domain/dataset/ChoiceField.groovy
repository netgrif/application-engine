package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.petrinet.domain.I18nString

abstract class ChoiceField<T> extends FieldWithDefault<T> {

    protected Set<I18nString> choices

    ChoiceField() {
        super()
        choices = new LinkedHashSet<I18nString>()
    }

    ChoiceField(List<I18nString> values) {
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

    void setChoicesFromStrings(Collection<String> choices) {
        this.choices = new LinkedHashSet<>()
        choices.each {
            this.choices.add(new I18nString(it))
        }
    }
}