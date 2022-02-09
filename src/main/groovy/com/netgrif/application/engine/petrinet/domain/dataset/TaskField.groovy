package com.netgrif.application.engine.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class TaskField extends Field<List<String>> {

    TaskField() {
        super()
        this.defaultValue = new ArrayList<>()
    }

    @Override
    FieldType getType() {
        return FieldType.TASK_REF
    }

    @Override
    Field clone() {
        TaskField clone = new TaskField()
        super.clone(clone)
        return clone
    }

    void setDefaultValue(List<String> defaultValue) {
        super.setDefaultValue(defaultValue)
    }
}
