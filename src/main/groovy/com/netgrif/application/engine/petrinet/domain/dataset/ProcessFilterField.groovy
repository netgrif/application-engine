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
        CaseFilterField clone = new CaseFilterField()
        super.clone(clone)
        return clone
    }
}
