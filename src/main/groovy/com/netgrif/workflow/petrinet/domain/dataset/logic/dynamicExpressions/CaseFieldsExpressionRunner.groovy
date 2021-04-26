package com.netgrif.workflow.petrinet.domain.dataset.logic.dynamicExpressions

import com.netgrif.workflow.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.workflow.workflow.domain.Case
import org.codehaus.groovy.control.CompilerConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.stereotype.Component

@Component
abstract class CaseFieldsExpressionRunner {

    private static final Logger log = LoggerFactory.getLogger(CaseFieldsExpressionRunner.class)

    @Autowired
    private CompilerConfiguration configuration

    @Lookup("actionDelegate")
    abstract ActionDelegate getActionDelegate()

    def run(Case useCase, String expression) {
        return run(useCase, useCase.getDataSet().keySet().collectEntries {[(it): (it)]} as Map<String, String>, expression)
    }

    def run(Case useCase, Map<String, String> fields, String expression) {
        logger().debug("Expression: $expression")
        def code = getActionCode(useCase, fields, expression)
        return code()
    }

    protected Closure getActionCode(Case useCase, Map<String, String> fields, String expression) {
        def code = (Closure) new GroovyShell(this.class.getClassLoader(), configuration).evaluate("{-> ${expression}}")
        code.delegate = getClosureDelegate(useCase, fields)
        return code
    }

    protected Object getClosureDelegate(Case useCase, Map<String, String> fields) {
        ActionDelegate ad = getActionDelegate()
        ad.useCase = useCase
        ad.initFieldsMap(fields)
        return ad
    }

    protected Logger logger() {
        return log
    }

}
