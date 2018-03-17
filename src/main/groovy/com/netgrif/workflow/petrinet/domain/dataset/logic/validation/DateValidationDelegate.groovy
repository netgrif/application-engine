package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.Field

import java.time.LocalDate

class DateValidationDelegate extends ValidationDelegate {

    DateValidationDelegate(Field field) {
        super(field)
    }

    protected static LocalDate parseDate(Object date) {
        if(date instanceof Closure)
            return date()
        if(date instanceof String)
            return LocalDate.parse(date)
        else
            return null
    }

    def future = { return LocalDate.MAX }

    def past = { return LocalDate.MIN }

    def today = { return LocalDate.now() }

    def between = { start, end ->
        LocalDate val = (LocalDate) field.value

        LocalDate startDate = parseDate(start)
        LocalDate endDate = parseDate(end)

        if(startDate == null || endDate == null)
            return false
        return val.isAfter(startDate) && val.isBefore(endDate)
    }

    def workday = {
        int dayOfWeek = ((LocalDate) field.value).getDayOfWeek().value
        return dayOfWeek >= 1 && dayOfWeek <= 5
    }

    def weekend = {
        int dayOfWeek = ((LocalDate) field.value).getDayOfWeek().value
        return dayOfWeek == 6 || dayOfWeek == 7
    }
}
