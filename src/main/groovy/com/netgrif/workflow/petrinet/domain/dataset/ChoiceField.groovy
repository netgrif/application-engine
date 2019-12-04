package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.petrinet.domain.I18nString

abstract class ChoiceField<T> extends FieldWithDefault<T> {

    protected Map<String, I18nString> choices

    ChoiceField() {
        super()
        choices = new LinkedHashMap<>()
    }

    ChoiceField(Map values) {
        this()
        if (values != null)
            this.choices = values
    }

    Map<String, I18nString> getChoices() {
        return choices
    }

    void setChoices(Map<String, I18nString> choices) {
        this.choices = choices
    }

    void setChoicesFromStrings(Collection<String> choices) {
        this.choices = new LinkedHashSet<>()
        choices.each {
            this.choices.add(new I18nString(it))
        }
    }
}