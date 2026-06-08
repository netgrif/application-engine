package com.netgrif.application.engine.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class ProcessFilterField extends Field<String> {

    ProcessFilterField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.PROCESS_FILTER
    }

    @Override
    Field clone() {
        ProcessFilterField clone = new ProcessFilterField()
        super.clone(clone)
        return clone
    }
}
