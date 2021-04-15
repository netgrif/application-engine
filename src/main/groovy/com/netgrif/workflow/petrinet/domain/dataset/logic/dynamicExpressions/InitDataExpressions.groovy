package com.netgrif.workflow.petrinet.domain.dataset.logic.dynamicExpressions

import com.netgrif.workflow.workflow.domain.Case
import org.springframework.stereotype.Component

@Component
class InitDataExpressions extends DataExpressions {

    def compile(Case useCase, String expression) {
        return compileClosure(useCase, expression)
    }

}
