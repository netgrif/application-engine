package com.netgrif.workflow.petrinet.domain.dataset.logic.dynamicExpressions

import com.netgrif.workflow.workflow.domain.Case
import org.springframework.stereotype.Component

import java.util.regex.Matcher

@Component
class InitDataExpressions extends DataExpressions {

    def compile(Case useCase, String expression) {
        Matcher matcher = EXPRESSION_REGEX.matcher(expression)
        matcher.find()
        return compileClean(useCase, matcher.group(1))
    }

    def compileClean(Case useCase, String expression) {
        return compileClosure(useCase, expression)
    }
}
