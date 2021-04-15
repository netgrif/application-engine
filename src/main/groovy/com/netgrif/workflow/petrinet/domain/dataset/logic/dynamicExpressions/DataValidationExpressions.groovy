package com.netgrif.workflow.petrinet.domain.dataset.logic.dynamicExpressions

import com.netgrif.workflow.workflow.domain.Case
import org.springframework.stereotype.Component

import java.util.regex.Matcher
import java.util.regex.Pattern

@Component
class DataValidationExpressions extends DataExpressions {

    /**
     * pattern for dynamic evaluation is ${code}
     */
    public static final Pattern EXPRESSION_REGEX = Pattern.compile("\\\$\\{((.|\n)[^\$]*)}")

    String compile(Case useCase, String expression) {
        Matcher matcher = EXPRESSION_REGEX.matcher(expression)
        while (matcher.find()) {
            String result = compileClosure(useCase, matcher.group(1)) as String
            expression = expression.replace(matcher.group(), result)
        }
        return expression
    }

    static boolean containsDynamicExpression(String expression) {
        Matcher matcher = EXPRESSION_REGEX.matcher(expression)
        return matcher.find()
    }

}
