package com.netgrif.workflow.petrinet.domain.dataset.logic.dynamicExpressions

import com.netgrif.workflow.workflow.domain.Case
import org.springframework.beans.factory.annotation.Autowired

abstract class DataExpressions {

    @Autowired
    protected CaseFieldsExpressionRunner expressionRunner

    protected def compileClosure(Case useCase, String expression) {
        return expressionRunner.run(useCase, expression)
    }

}
