package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.FieldType


class UnsupportedFieldTypeException extends RuntimeException{
    UnsupportedFieldTypeException(FieldType type) {
        super("Unsupported field type ${type.name}. Type ${type.name} has not defined validation DSL")
    }
}
