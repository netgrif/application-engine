package com.netgrif.workflow.petrinet.domain.dataset.logic.dynamicExpressions

import com.netgrif.workflow.workflow.domain.Case
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DataExpressions {

    @Autowired
    protected CaseFieldsExpressionRunner expressionRunner

    String compile(Case useCase, Expression expression) {
        return expressionRunner.run(useCase, expression)?.toString()
    }

}
