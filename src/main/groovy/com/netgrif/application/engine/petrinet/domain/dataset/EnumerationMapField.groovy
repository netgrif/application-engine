package com.netgrif.application.engine.petrinet.domain.dataset


import com.netgrif.application.engine.petrinet.domain.I18nString
import org.springframework.data.mongodb.core.mapping.Document

@Document
class EnumerationMapField extends MapOptionsField<I18nString, String> {

    EnumerationMapField() {
        super()
    }

    EnumerationMapField(Map<String, I18nString> options) {
        super(options)
    }

    EnumerationMapField(Map<String, I18nString> options, String defaultValue) {
        super(options)
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

    // todo 2483 doc
    I18nString getI18nValue() {
        // todo 2483 test
        if (this.getValue() == null) {
            return null;
        }
        return this.options?.get(this.getValue())
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
