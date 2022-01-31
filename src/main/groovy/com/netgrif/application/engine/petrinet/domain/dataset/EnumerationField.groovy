package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.petrinet.domain.I18nString
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
        super.setValue(value)
        //TODO: case save choices
//        if (!value || choices.find { (it == value) }) {
//            super.setValue(value)
//        } else {
//            throw new IllegalArgumentException("Value $value is not a choice")
//        }
    }

    void setValue(String value) {
        def i18n = choices.find { it.contains(value) }
        //TODO: case save choices
        if (i18n == null)
            i18n = new I18nString(value)
        super.setValue(i18n)
    }

    void setDefaultValue(String defaultValue) {
        I18nString value = choices.find { it.contains(defaultValue) }
        if (!value && defaultValue)
            value = new I18nString(defaultValue)
        //TODO: case save choices
//            throw new IllegalArgumentException("Value $defaultValue is not a choice.")
        this.defaultValue = value
    }

    String getTranslatedValue(Locale locale) {
        return value?.getTranslation(locale)
    }

    @Override
    Field clone() {
        EnumerationField clone = new EnumerationField()
        super.clone(clone)
        clone.choices = this.choices
        clone.choicesExpression = this.choicesExpression
        return clone
    }
}