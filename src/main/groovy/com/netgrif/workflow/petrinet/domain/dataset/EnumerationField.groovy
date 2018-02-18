package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.petrinet.domain.I18nString
import org.springframework.data.mongodb.core.mapping.Document

@Document
class EnumerationField extends ChoiceField<I18nString> {

    EnumerationField() {
        super()
    }

    EnumerationField(List<I18nString> values) {
        super(values)
    }

    @Override
    FieldType getType() {
        return FieldType.ENUMERATION
    }

    @Override
    void setValue(I18nString value) {
        if (!value || choices.find { (it == value) }) {
            super.setValue(value)
        } else {
            throw new IllegalArgumentException("Value $value is not a choice")
        }
    }

    void setValue(String value) {
        def i18n = choices.find {it.contains(value)}
        setValue(i18n)
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

    void setDefaultValue(String defaultValue) {
        I18nString value = choices.find { it.contains(defaultValue) }
        if (!value && defaultValue)
            throw new IllegalArgumentException("Value $defaultValue is not a choice.")
        this.defaultValue = value
    }

    String getTranslatedValue(Locale locale) {
        return value?.getTranslation(locale)
    }
}