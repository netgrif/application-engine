package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.DateField
import com.netgrif.workflow.petrinet.domain.dataset.Field

import java.time.LocalDate

class DateValidationJavascriptDelegate extends DateValidationDelegate{

    DateValidationJavascriptDelegate(Field field) {
        super(field)
    }

    def between = { start, end ->
        DateField field = (DateField)this.field

        LocalDate startDate = parseDate(start)
        LocalDate endDate = parseDate(end)

        if(startDate == null || endDate == null)
            return ""

        if(startDate.equals(past())) {
            field.setMaxDate(endDate.toString())
            return "const endDate = new Date('${endDate.toString()}'); ${setupJavascriptValidation("between","(value - endDate) > 86400000")}"
        }
        if(endDate.equals(future())) {
            field.setMinDate(startDate.toString())
            return "const startDate = new Date('${startDate.toString()}'); ${setupJavascriptValidation( "between","(value - startDate) < -86400000")}"
        }
        field.setMinDate(startDate.toString())
        field.setMaxDate(endDate.toString())
        return "const endDate = new Date('${endDate.toString()}'); const startDate = new Date('${startDate.toString()}'); ${setupJavascriptValidation("between","(value - endDate) > 86400000 || (value - startDate) < -86400000")}"
    }

    def workday = {
        "const dayOfWeek = value.getDay(); ${setupJavascriptValidation("workday","dayOfWeek === 6 || dayOfWeek === 0")}"
    }

    def weekend = {
        "const dayOfWeek = value.getDay(); ${setupJavascriptValidation("weekend","dayOfWeek >= 1 && dayOfWeek <= 5 && dayOfWeek !== 0")}"
    }
}
