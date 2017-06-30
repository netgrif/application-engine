package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.Field


class NumberValidationJavascriptDelegate extends NumberValidationDelegate{

    NumberValidationJavascriptDelegate(Field field) {
        super(field)
    }

    def odd = { "if((field.value % 2) === 0) return false;" }

    def even = { "if((field.value % 2) !== 0) return false;" }

    def positive = { "if(field.value < 0) return false;" }

    def negative = { "if(field.value >= 0) return false;" }

    def decimal = { "if((field.value % 1) !== 0) return false;" }

    def inrange = { n, m ->
        if (n instanceof Closure && n() == INFINITY) return "if(field.value > ${m}) return false;"
        if (m instanceof Closure && m() == INFINITY) return "if(field.value < ${n}) return false;"
        return "if(field.value < ${n} || field.value > ${m}) return false;"
    }
}
