package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class TaskField extends FieldWithDefault<List<String>> {

    @Override
    FieldType getType() {
        return FieldType.TASK_REF
    }

    @Override
    Field clone() {
        TaskField clone = new TaskField()
        super.clone(clone)
        clone.defaultValue = this.defaultValue
        clone.initExpression = this.initExpression
        return clone
    }

    void setDefaultValue(List<String> defaultValue) {
        super.setDefaultValue(defaultValue)
    }
}
