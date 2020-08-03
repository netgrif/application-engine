package com.netgrif.workflow.petrinet.domain.dataset


import com.netgrif.workflow.petrinet.domain.I18nString
import org.springframework.data.mongodb.core.mapping.Document

@Document
class EnumerationMapField extends MapOptionsField<I18nString, String> {

    EnumerationMapField() {
        super()
    }

    EnumerationMapField(Map<String, I18nString> choices) {
        super(choices)
    }

    EnumerationMapField(Map<String, I18nString> choices, String defaultValue) {
        super(choices)
        this.defaultValue = defaultValue
    }

    @Override
    FieldType getType() {
        return FieldType.ENUMERATION_MAP
    }

    @Override
    Map<String, I18nString> getOptions() {
        return super.getOptions()
    }

    @Override
    void setOptions(Map<String, I18nString> choices) {
        super.setOptions(choices)
    }

    @Override
    String getDefaultValue() {
        return super.getDefaultValue()
    }

    @Override
    void setDefaultValue(String defaultValue) {
        super.setDefaultValue(defaultValue)
    }

    @Override
    Field clone() {
        EnumerationMapField clone = new EnumerationMapField()
        super.clone(clone)

        clone.options = options
        clone.defaultValue = defaultValue

        return clone
    }
}
