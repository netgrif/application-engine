package com.netgrif.workflow.petrinet.domain.dataset.logic.action.runner

import com.netgrif.workflow.elastic.service.executors.MaxSizeHashMap
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.ActionDelegate
import org.codehaus.groovy.control.CompilerConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

abstract class FieldsExpressionRunner<ENTITY> {

    private static final Logger log = LoggerFactory.getLogger(FieldsExpressionRunner.class)

    @Autowired
    private CompilerConfiguration configuration

    private int cacheSize

    private Map<String, Closure> cache = new MaxSizeHashMap<>(cacheSize)

    FieldsExpressionRunner(int cacheSize) {
        this.cacheSize = cacheSize
    }

    def run(ENTITY entity, Expression expression) {
        return run(entity, getFieldIds(entity), expression)
    }

    def run(ENTITY entity, Map<String, String> fields, Expression expression) {
        log.debug("Expression: $expression")
        def code = getExpressionCode(expression)
        try {
            initCode(code.delegate, entity, fields)
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
            code = (Closure) new GroovyShell(this.class.getClassLoader(), configuration).evaluate("{-> ${expression.definition}}")
            cache.put(expression.stringId, code)
        }
        return code.rehydrate(actionDelegate(), code.owner, code.thisObject)
    }

    protected abstract void initCode(Object delegate, ENTITY entity, Map<String, String> fields)

    protected abstract Map<String, String> getFieldIds(ENTITY entity)

    protected abstract ActionDelegate actionDelegate()
}
