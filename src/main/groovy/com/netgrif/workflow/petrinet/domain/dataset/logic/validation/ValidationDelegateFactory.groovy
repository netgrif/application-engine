package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.FieldType


class ValidationDelegateFactory {


    static ValidationDelegate getDelegate(Field field){
        switch (field.type){
            case FieldType.NUMBER:
                return new NumberValidationDelegate(field)
            case FieldType.TEXT:
                return new TextValidationDelegate(field)
            case FieldType.DATE:
                return new DateValidationDelegate(field)
            default:
                throw new UnsupportedFieldTypeException(field.type)
        }
    }

}
