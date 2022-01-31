package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner

import com.netgrif.application.engine.event.IGroovyShellFactory
import com.netgrif.application.engine.elastic.service.executors.MaxSizeHashMap
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.workflow.domain.Case
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
abstract class CaseFieldsExpressionRunner {

    private static final Logger log = LoggerFactory.getLogger(CaseFieldsExpressionRunner.class)

    @Lookup("actionDelegate")
    abstract ActionDelegate getActionDelegate()

    @Autowired
    private IGroovyShellFactory shellFactory

    private int cacheSize

    private Map<String, Closure> cache = new MaxSizeHashMap<>(cacheSize)

    @Autowired
    CaseFieldsExpressionRunner(@Value('${nae.expressions.runner.cache-size}') int cacheSize) {
        this.cacheSize = cacheSize
    }

    def run(Case useCase, Expression expression) {
        return run(useCase, useCase.getDataSet().keySet().collectEntries { [(it): (it)] } as Map<String, String>, expression)
    }

    def run(Case useCase, Map<String, String> fields, Expression expression) {
        logger().debug("Expression: $expression")
        def code = getExpressionCode(expression)
        try {
            initCode(code.delegate, useCase, fields)
            code()
        } catch (Exception e) {
            log.error("Action: $expression.definition")
            throw e
        }
    }

    protected Closure getExpressionCode(Expression expression) {
        def code
        if (cache.containsKey(expression.stringId)) {
            code = cache.get(expression.stringId)
        } else {
            code = (Closure) this.shellFactory.getGroovyShell().evaluate("{-> ${expression.definition}}")
            cache.put(expression.stringId, code)
        }
        return code.rehydrate(getActionDelegate(), code.owner, code.thisObject)
    }

    protected void initCode(Object delegate, Case useCase, Map<String, String> fields) {
        ActionDelegate ad = ((ActionDelegate) delegate)
        ad.useCase = useCase
        ad.initFieldsMap(fields)
    }

    protected Logger logger() {
        return log
    }

}
