package com.netgrif.application.engine.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class TaskFilterField extends Field<String> {

    TaskFilterField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.TASK_FILTER
    }

    @Override
    Field clone() {
        TaskFilterField clone = new TaskFilterField()
        super.clone(clone)
        return clone
    }
}
