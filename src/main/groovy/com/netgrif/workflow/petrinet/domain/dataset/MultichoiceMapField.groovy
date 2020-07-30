package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.petrinet.domain.I18nString
import org.springframework.data.mongodb.core.mapping.Document

@Document
class MultichoiceMapField extends MapChoiceField<I18nString, Set<String>> {

    MultichoiceMapField() {
        super()
    }

    MultichoiceMapField(Map<String, I18nString> choices) {
        super(choices)
    }

    MultichoiceMapField(Map<String, I18nString> choices, Set<String> defaultValues) {
        super(choices)
        this.defaultValue = defaultValues
    }

    @Override
    FieldType getType() {
        return FieldType.MULTICHOICE_MAP
    }

    @Override
    Map<String, I18nString> getChoices() {
        return super.getChoices()
    }

    @Override
    void setChoices(Map<String, I18nString> choices) {
        super.setChoices(choices)
    }

    @Override
    Set<String> getDefaultValue() {
        return super.getDefaultValue() as Set<String>
    }

    @Override
    void setDefaultValue(Set<String> defaultValue) {
        super.setDefaultValue(defaultValue)
    }

    @Override
    Field clone() {
        MultichoiceMapField clone = new MultichoiceMapField()
        super.clone(clone)

        clone.choices = choices
        clone.defaultValue = defaultValue

        return clone
    }
}
