package com.netgrif.workflow.petrinet.domain.dataset.logic.logic

import com.netgrif.workflow.workflow.domain.Case


class FieldLogic {

    protected Case useCase

    FieldLogic(Case useCase) {
        this.useCase = useCase
    }
}
