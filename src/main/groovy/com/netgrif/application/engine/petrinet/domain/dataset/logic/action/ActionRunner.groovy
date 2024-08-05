package com.netgrif.application.engine.petrinet.domain.dataset.logic.action

import com.netgrif.application.engine.petrinet.domain.Function
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.transaction.NaeTransaction
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.EventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionSynchronizationManager

@Slf4j
@Component
@SuppressWarnings("GrMethodMayBeStatic")
abstract class ActionRunner {

    @Lookup("actionDelegate")
    abstract ActionDelegate getActionDelegate()

    @Autowired
    private IFieldActionsCacheService actionsCacheService

    @Autowired
    private MongoTransactionManager transactionManager

    private Map<String, Object> actionsCache = new HashMap<>()

    List<EventOutcome> run(Action action, Case useCase,  Map<String, String> params, List<Function> functions = []) {
        return run(action, useCase, Optional.empty(), null, params, functions)
    }

    List<EventOutcome> run(Action action, Case useCase, Optional<Task> task, Field<?> changes, Map<String, String> params, List<Function> functions = []) {
        if (!actionsCache) {
            actionsCache = new HashMap<>()
        }
        log.debug("Action: $action")
        def code = getActionCode(action, functions)
        try {
            code.init(action, useCase, task, changes, this, params)
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                def transaction = NaeTransaction.builder()
                        .timeout(TransactionDefinition.TIMEOUT_DEFAULT)
                        .forceCreation(false)
                        .transactionManager(transactionManager)
                        .event(code)
                        .build()
                transaction.setForceCreation(false)
                transaction.begin()
            } else {
                code()
            }
        } catch (Exception e) {
            log.error("Action: $action.definition")
            throw e
        }
        return ((ActionDelegate) code.delegate).outcomes
    }

    Closure getActionCode(Action action, List<Function> functions, boolean shouldRewriteCachedActions = false) {
        return getActionCode(actionsCacheService.getCompiledAction(action, shouldRewriteCachedActions), functions)
    }

    Closure getActionCode(Closure code, List<Function> functions) {
        def actionDelegate = getActionDelegate()

        actionsCacheService.getCachedFunctions(functions).each {
            actionDelegate.metaClass."${it.function.name}" << it.code
        }
        actionsCacheService.getNamespaceFunctionCache().each { entry ->
            def namespace = [:]
            entry.getValue().each {
                namespace["${it.function.name}"] = it.code.rehydrate(actionDelegate, it.code.owner, it.code.thisObject)
            }
            actionDelegate.metaClass."${entry.key}" = namespace
        }
        return code.rehydrate(actionDelegate, code.owner, code.thisObject)
    }

    void addToCache(String key, Object value) {
        this.actionsCache.put(key, value)
    }

    void removeFromCache(String key) {
        this.actionsCache.remove(key)
    }

    def getFromCache(String key) {
        return this.actionsCache.get(key)
    }
}