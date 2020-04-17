package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.petrinet.domain.I18nString

abstract class ChoiceField<T> extends FieldWithDefault<T> {

    protected Map<String, I18nString> choices

    ChoiceField() {
        super()
        choices = new LinkedHashMap<>()
    }

    ChoiceField(Map<String, I18nString> values) {
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

    void setChoices(Collection<I18nString> choices) {
        this.choices.clear()
        choices.each { this.choices.put(it.defaultValue, it) }
    }

    void setChoicesFromStrings(Map<String, String> choices) {
        choices.each { it.value = new I18nString(it.value) }
        this.choices = choices
    }

    void setChoicesFromStrings(Collection<String> choices) {
        this.choices.clear()
        choices.each { this.choices.put(it, new I18nString(it)) }
    }
}