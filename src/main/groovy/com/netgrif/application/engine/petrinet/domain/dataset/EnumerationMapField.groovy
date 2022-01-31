package com.netgrif.application.engine.petrinet.domain.dataset


import com.netgrif.application.engine.petrinet.domain.I18nString
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
    void setOptions(Map<String, I18nString> options) {
        super.setOptions(options)
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
        clone.optionsExpression = optionsExpression
        return clone
    }
}
