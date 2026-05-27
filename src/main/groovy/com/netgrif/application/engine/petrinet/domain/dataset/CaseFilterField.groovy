package com.netgrif.application.engine.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class CaseFilterField extends Field<String> {

    CaseFilterField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.CASE_FILTER
    }

    @Override
    Field clone() {
        CaseFilterField clone = new CaseFilterField()
        super.clone(clone)
        return clone
    }
}
