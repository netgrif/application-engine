package com.netgrif.workflow.petrinet.domain.dataset.logic.dynamicExpressions

import com.netgrif.workflow.workflow.domain.Case
import org.springframework.stereotype.Component

import java.util.regex.Matcher
import java.util.regex.Pattern

@Component
class InitDataExpressions extends DataExpressions {

    protected static final Pattern EXPRESSION_REGEX = Pattern.compile("\\\$\\{((.|\n)*)}")

    def compile(Case useCase, String expression) {
        Matcher matcher = EXPRESSION_REGEX.matcher(expression)
        matcher.find()
        return compileClean(useCase, matcher.group(1))
    }

    def compileClean(Case useCase, String expression) {
        return compileClosure(useCase, expression)
    }

    static boolean containsDynamicExpression(String expression) {
        Matcher matcher = EXPRESSION_REGEX.matcher(expression)
        return matcher.find()
    }

}
