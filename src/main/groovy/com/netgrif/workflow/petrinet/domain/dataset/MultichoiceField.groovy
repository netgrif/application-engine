package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class MultichoiceField extends ChoiceField<Set<String>> {

    MultichoiceField() {
        super()
        value = new HashSet<>()
    }

    MultichoiceField(List<String> values) {
        super(values)
    }

    @Override
    FieldType getType() {
        return FieldType.MULTICHOICE
    }

    @Override
    void setDefaultValue(String value) {
        String[] vls = value.split(",")
        vls.each { s -> s.trim() }
        this.defaultValue = new HashSet<String>(vls as Set)
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

    void setValue(List<String> value) {
        this.value = new HashSet<>(value)
    }
}