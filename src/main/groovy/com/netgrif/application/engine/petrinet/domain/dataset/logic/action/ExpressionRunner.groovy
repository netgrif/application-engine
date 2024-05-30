package com.netgrif.application.engine.petrinet.domain.dataset.logic.action

import com.netgrif.application.engine.elastic.service.executors.MaxSizeHashMap
import com.netgrif.application.engine.event.IGroovyShellFactory
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression
import com.netgrif.application.engine.workflow.domain.Case
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Slf4j
@Component
abstract class ExpressionRunner {

    // TODO: release/8.0.0 move methods to expression delegate, extended by action delegate
//    @Lookup("expressionDelegate")
//    abstract ExpressionDelegate getExpressionDelegate()
    @Lookup("actionDelegate")
    abstract ActionDelegate getExpressionDelegate()


    @Autowired
    private IGroovyShellFactory shellFactory

    private Map<String, Closure> cache

    @Autowired
    ExpressionRunner(@Value('${nae.expressions.runner.cache-size}') int cacheSize) {
        cache = new MaxSizeHashMap<>(cacheSize)
    }

    // TODO: release/8.0.0 fields? Map<String, String> fields
    def run(Case useCase, Expression expression, Map<String, String> params = [:]) {
        log.debug("Expression: $expression")
        def code = getExpressionCode(expression)
        try {
            initCode(code.delegate, useCase, params)
            code()
        } catch (Exception e) {
            log.error("Action: $expression.definition")
            throw e
        }
    }

    protected Closure getExpressionCode(Expression expression) {
        def code
        if (cache.containsKey(expression.id)) {
            code = cache.get(expression.id)
        } else {
            code = (Closure) this.shellFactory.getGroovyShell().evaluate("{-> ${expression.definition}}")
            cache.put(expression.id, code)
        }
        return code.rehydrate(getExpressionDelegate(), code.owner, code.thisObject)
    }

    protected void initCode(def delegate, Case useCase, Map<String, String> params) {
        delegate.metaClass.useCase = useCase
        delegate.metaClass.params = params
        useCase.dataSet.fields.values().forEach { Field<?> field ->
            delegate.metaClass."$field.importId" = field
        }
    }
}
