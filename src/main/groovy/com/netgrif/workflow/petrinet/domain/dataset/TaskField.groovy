package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class TaskField extends Field<String> {

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
}
