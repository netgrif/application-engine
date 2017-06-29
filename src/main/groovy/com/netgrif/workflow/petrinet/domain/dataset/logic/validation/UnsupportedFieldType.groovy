package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.FieldType


class UnsupportedFieldType extends RuntimeException{
    UnsupportedFieldType(FieldType type) {
        super("Unsupported field type ${type.name}. Type ${type.name} has not defined validation DSL")
    }
}
