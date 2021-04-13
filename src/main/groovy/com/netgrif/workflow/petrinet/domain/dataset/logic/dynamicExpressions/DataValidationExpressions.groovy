package com.netgrif.workflow.petrinet.domain.dataset.logic.dynamicExpressions

import com.netgrif.workflow.workflow.domain.Case
import org.springframework.stereotype.Component

import java.util.regex.Matcher

@Component
class DataValidationExpressions extends DataExpressions {

    String compile(Case useCase, String expression) {
        Matcher matcher = EXPRESSION_REGEX.matcher(expression)
        while (matcher.find()) {
            String result = compileClosure(useCase, matcher.group(1)) as String
            expression = expression.replace(matcher.group(), result)
        }
        return expression
    }

}
