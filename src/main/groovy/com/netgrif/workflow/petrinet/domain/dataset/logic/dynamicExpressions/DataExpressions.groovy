package com.netgrif.workflow.petrinet.domain.dataset.logic.dynamicExpressions

import com.netgrif.workflow.workflow.domain.Case
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.regex.Matcher
import java.util.regex.Pattern

@Component
class DataExpressions {

    /**
     * pattern for dynamic evaluation is ${code}
     */
    public static final Pattern EXPRESSION_REGEX = Pattern.compile("\\\$\\{((.|\n)*?)}")

    @Autowired
    protected CaseFieldsExpressionRunner expressionRunner

    String compile(Case useCase, String expression) {
        Matcher matcher = EXPRESSION_REGEX.matcher(expression)
        while (matcher.find()) {
            String result = compileClosure(useCase, matcher.group(1))
            expression = expression.replace(matcher.group(), result)
        }
        return expression
    }

    static boolean containsDynamicExpression(String expression) {
        Matcher matcher = EXPRESSION_REGEX.matcher(expression)
        return matcher.find()
    }

    protected String compileClosure(Case useCase, String expression) {
        return expressionRunner.run(useCase, expression)?.toString()
    }

    protected static String extractDynamicExpression(String expression) {
        Matcher matcher = EXPRESSION_REGEX.matcher(expression)
        return matcher.group()
    }

}
