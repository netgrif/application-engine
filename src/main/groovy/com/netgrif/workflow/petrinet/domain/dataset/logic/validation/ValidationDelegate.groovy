package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.Field


abstract class ValidationDelegate {

    protected Field field

    ValidationDelegate(Field field) {
        this.field = field
    }

    def validate(Closure... cls){
        for(cl in cls){
            if(!cl()) return false
        }
        return true
    }

    def javascript(Closure... cls){
        StringBuilder builder = new StringBuilder()
        for(cl in cls){
            builder.append(cl())
        }
        builder.append("return true;")
        return builder.toString()
    }
}
