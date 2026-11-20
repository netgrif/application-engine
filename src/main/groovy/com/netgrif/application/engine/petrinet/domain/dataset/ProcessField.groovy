package com.netgrif.application.engine.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class ProcessField extends Field<List<String>> {

    ProcessField() {
        super()
        super.defaultValue = new ArrayList()
    }

    @Override
    FieldType getType() {
        return FieldType.PROCESS_REF
    }

    @Override
    void clearValue() {
        this.setValue(new ArrayList<String>())
    }

    @Override
    Field clone() {
        ProcessField clone = new ProcessField()
        super.clone(clone)
        return clone
    }

    void setDefaultValue(List<String> defaultValue) {
        super.setDefaultValue(defaultValue)
    }
}