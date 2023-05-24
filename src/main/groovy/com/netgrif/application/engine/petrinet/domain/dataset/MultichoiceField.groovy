package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.petrinet.domain.I18nString
import org.springframework.data.mongodb.core.mapping.Document

@Document
class MultichoiceField extends ChoiceField<LinkedHashSet<I18nString>> {

    MultichoiceField() {
        super()
        super.setValue(new LinkedHashSet<I18nString>())
        super.setDefaultValue(new LinkedHashSet<I18nString>())
    }

    MultichoiceField(List<I18nString> values) {
        super(values)
        super.setValue(new LinkedHashSet<I18nString>())
        super.setDefaultValue(new LinkedHashSet<I18nString>())
    }

    @Override
    FieldType getType() {
        return FieldType.MULTICHOICE
    }

    void setDefaultValue(String value) {
        if (value == null) {
            this.defaultValue = null
        } else {
            String[] vls = value.split(",")
            def defaults = new LinkedHashSet()
            vls.each { s ->
                defaults << choices.find { it ->
                    it.defaultValue == s.trim()
                }
            }
            super.setDefaultValue(defaults)
        }
    }

    void setDefaultValues(List<String> inits) {
        if (inits == null || inits.isEmpty()) {
            this.defaultValue = null
        } else {
            Set<I18nString> defaults = new LinkedHashSet<>()
            inits.forEach { initValue ->
                defaults << choices.find { choice ->
                    choice.defaultValue == initValue.trim()
                }
            }
            super.setDefaultValue(defaults)
        }
    }

    void setValue(String value) {
        I18nString i18n = choices.find { it.contains(value) }
        if (i18n == null && value != null)
            i18n = new I18nString(value)
        //TODO: case save choices
//            throw new IllegalArgumentException("Value $value is not a choice")
        super.setValue([i18n] as Set)
    }

    void setValue(Collection<String> values) {
        def newValues = [] as Set
        for (String value : values) {
            I18nString i18n = choices.find { it.contains(value) }
            if (i18n == null && value != null)
                i18n = new I18nString(value)
            //TODO: case save choices
//                throw new IllegalArgumentException("Value $value is not a choice")
            newValues << i18n
        }
        super.setValue(newValues)
    }

    @Override
    void setValue(LinkedHashSet<I18nString> value) {
        super.setValue(value)
    }


    @Override
    Field clone() {
        MultichoiceField clone = new MultichoiceField()
        super.clone(clone)
        clone.choices = this.choices
        clone.choicesExpression = this.choicesExpression
        return clone
    }
}