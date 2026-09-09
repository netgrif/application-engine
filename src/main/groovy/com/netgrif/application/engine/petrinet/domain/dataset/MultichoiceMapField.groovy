package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.petrinet.domain.I18nString
import org.springframework.data.mongodb.core.mapping.Document

@Document
class MultichoiceMapField extends MapOptionsField<I18nString, LinkedHashSet<String>> {

    MultichoiceMapField() {
        super()
        this.defaultValue = new LinkedHashSet<>()
    }

    MultichoiceMapField(Map<String, I18nString> options) {
        super(options)
        this.defaultValue = new LinkedHashSet<>()
    }

    MultichoiceMapField(Map<String, I18nString> options, LinkedHashSet<String> defaultValues) {
        this(options)
        this.defaultValue = defaultValues
    }

    @Override
    FieldType getType() {
        return FieldType.MULTICHOICE_MAP
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
    LinkedHashSet<String> getDefaultValue() {
        return super.getDefaultValue() as Set<String>
    }

    @Override
    void setDefaultValue(LinkedHashSet<String> defaultValue) {
        super.setDefaultValue(defaultValue)
    }

    // todo 2483 doc
    Set<I18nString> getI18nValue() {
        // todo 2483 test
        if (this.options == null || this.options.isEmpty() || this.getValue() == null) {
            return new LinkedHashSet<>()
        }
        return this.getValue().collect { this.options[it] } as LinkedHashSet
    }

    @Override
    Field clone() {
        MultichoiceMapField clone = new MultichoiceMapField()
        super.clone(clone)
        clone.options = options
        clone.optionsExpression = optionsExpression
        return clone
    }
}
