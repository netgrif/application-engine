package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.DateField
import com.netgrif.workflow.petrinet.domain.dataset.Field

class DateValidationJavascriptDelegate extends DateValidationDelegate{

    DateValidationJavascriptDelegate(Field field) {
        super(field)
    }

    def between = { start, end ->
        DateField field = (DateField)this.field
        if(start instanceof Closure && start() == INFINITY) {
            field.setMaxDate((String)end)
            return "const endDate = new Date('${end}'); ${setupJavascriptValidation("between","(value - endDate) > 0")}"
        }
        if(end instanceof Closure && end() == INFINITY) {
            field.setMinDate((String)start)
            return "const startDate = new Date('${start}'); ${setupJavascriptValidation( "between","(value - startDate) < 0")}"
        }
        field.setMinDate((String)start)
        field.setMaxDate((String)end)
        return "const endDate = new Date('${end}'); const startDate = new Date('${start}'); ${setupJavascriptValidation("between","(value - endDate) > 0 || (value - startDate) < 0")}"
    }

    def workday = {
        "const dayOfWeek = value.getDay(); ${setupJavascriptValidation("workday","dayOfWeek === 6 || dayOfWeek === 0")}"
    }

    def weekend = {
        "const dayOfWeek = value.getDay(); ${setupJavascriptValidation("weekend","dayOfWeek >= 1 && dayOfWeek <= 5 && dayOfWeek !== 0")}"
    }
}
