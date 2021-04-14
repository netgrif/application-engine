package com.netgrif.workflow.petrinet.domain.dataset.logic.dynamicExpressions

import com.netgrif.workflow.workflow.domain.Case
import org.springframework.beans.factory.annotation.Autowired

import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class DataExpressions {

    /**
     * pattern for dynamic evaluation is ${code}
     */
    protected static final Pattern EXPRESSION_REGEX = Pattern.compile("\\\$\\{((.|\n)[^\$]*)}")

    @Autowired
    protected CaseFieldsExpressionRunner expressionRunner

    static boolean containsDynamicExpression(String expression) {
        Matcher matcher = EXPRESSION_REGEX.matcher(expression)
        return matcher.find()
    }

    protected def compileClosure(Case useCase, String expression) {
        return expressionRunner.run(useCase, expression)
    }

}
