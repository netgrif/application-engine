package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner

import com.netgrif.application.engine.configuration.properties.NaeExpressionsProperties
import com.netgrif.application.engine.elastic.service.executors.MaxSizeHashMap
import com.netgrif.application.engine.event.IGroovyShellFactory
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.workflow.domain.Case
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.stereotype.Component

@Slf4j
@Component
abstract class CaseFieldsExpressionRunner {

    @Lookup("actionDelegate")
    abstract ActionDelegate getActionDelegate()

    @Autowired
    private IGroovyShellFactory shellFactory

    @Autowired
    private NaeExpressionsProperties naeExpressionsProperties

    private int cacheSize

    private Map<String, Closure> cache = new MaxSizeHashMap<>(cacheSize)

    @Autowired
    CaseFieldsExpressionRunner() {
        this.cacheSize = naeExpressionsProperties.runner.cacheSize
    }

    def run(Case useCase, Expression expression) {
        log.debug("Expression: $expression")
        def code = getExpressionCode(expression)
        try {
            initCode(code.delegate, useCase)
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

    protected void initCode(Object delegate, Case useCase) {
        ActionDelegate ad = ((ActionDelegate) delegate)
        ad.useCase = useCase
        ad.initFieldsMap(useCase.dataSet.fields.collectEntries { [(it.key): (it.key)] }, useCase)
    }
}
