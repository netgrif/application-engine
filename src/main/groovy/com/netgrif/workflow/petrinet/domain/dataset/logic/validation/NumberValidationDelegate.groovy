package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.Field


class NumberValidationDelegate extends ValidationDelegate {

    protected static final int INFINITY = 0

    NumberValidationDelegate(Field field) {
        super(field)
    }

    def odd = { field.value % 2 }

    def even = { !(field.value % 2) }

    def positive = { field.value >= 0 }

    def negative = { field.value < 0 }

    def decimal = { field.value % 1 == 0 }

    def inf = { return INFINITY }

    def inrange = { n, m ->
        if (n instanceof Closure && n() == INFINITY) return field.value <= m
        if (m instanceof Closure && m() == INFINITY) return field.value >= n
        return field.value >= n && field.value <= m
    }
}
