package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.NumberField


class NumberValidationJavascriptDelegate extends NumberValidationDelegate{

    NumberValidationJavascriptDelegate(Field field) {
        super(field)
    }

    def odd = { setupJavascriptValidation("odd","(value % 2) === 0") }

    def even = { setupJavascriptValidation("even","(value % 2) !== 0") }

    def positive = { setupJavascriptValidation("positive","value < 0") }

    def negative = { setupJavascriptValidation("negative","value >= 0") }

    def decimal = { setupJavascriptValidation("decimal", "value % 1 !== 0") }

    def inrange = { n, m ->
        NumberField field = (NumberField)this.field
        if (n instanceof Closure && n() == INFINITY) {
            field.setMaxValue((Double)m)
            return setupJavascriptValidation("inrange","value > ${m}")
        }
        if (m instanceof Closure && m() == INFINITY) {
            field.setMinValue((Double)n)
            return setupJavascriptValidation("inrange","value < ${n}")
        }
        field.setMinValue((Double)n)
        field.setMaxValue((Double)m)
        return setupJavascriptValidation("inrange","value < ${n} || value > ${m}")
    }
}
