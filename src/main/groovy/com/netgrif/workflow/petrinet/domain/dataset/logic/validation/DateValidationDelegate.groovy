package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.Field

import java.time.LocalDate

class DateValidationDelegate extends ValidationDelegate{

    protected static final int INFINITY = 0

    DateValidationDelegate(Field field) {
        super(field)
    }

    def inf = { return INFINITY }

    def between = { start, end ->
        LocalDate val = (LocalDate)field.value
        if(start instanceof Closure && start() == INFINITY) return val.isBefore(LocalDate.parse((String)end))
        if(end instanceof Closure && end() == INFINITY) return val.isAfter(LocalDate.parse((String)start))
        return val.isAfter(LocalDate.parse((String)start)) && val.isBefore(LocalDate.parse((String)end))
    }

    def workday = {
        int dayOfWeek = ((LocalDate)field.value).getDayOfWeek().value
        return dayOfWeek >= 1 && dayOfWeek <= 5
    }

    def weekend = {
        int dayOfWeek = ((LocalDate)field.value).getDayOfWeek().value
        return dayOfWeek == 6 || dayOfWeek == 7
    }
}
