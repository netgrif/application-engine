package com.netgrif.application.engine.petrinet.domain.dataset.logic.action

import com.netgrif.application.engine.business.IPostalCodeService
import com.netgrif.application.engine.business.orsr.IOrsrService
import com.netgrif.application.engine.importer.service.FieldFactory
import com.netgrif.application.engine.petrinet.domain.Function
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.stereotype.Component

@Component
@SuppressWarnings("GrMethodMayBeStatic")
abstract class FieldActionsRunner {

    private static final Logger log = LoggerFactory.getLogger(FieldActionsRunner.class)

    @Lookup("actionDelegate")
    abstract ActionDelegate getActionDeleget()

    @Autowired
    private IOrsrService orsrService

    @Autowired
    private IPostalCodeService postalCodeService

    @Autowired
    private FieldFactory fieldFactory

    @Autowired
    private IFieldActionsCacheService actionsCacheService

    private Map<String, Object> actionsCache = new HashMap<>()

    List<EventOutcome> run(Action action, Case useCase, List<Function> functions = []) {
        return run(action, useCase, Optional.empty(), functions)
    }

    List<EventOutcome> run(Action action, Case useCase, Optional<Task> task, List<Function> functions = []) {
        if (!actionsCache)
            actionsCache = new HashMap<>()

        log.debug("Action: $action")
        def code = getActionCode(action, functions)
        try {
            code.init(action, useCase, task, this)
            code()
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
        def actionDelegate = getActionDeleget()

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

    IPostalCodeService getPostalCodeService() {
        return postalCodeService
    }

    IOrsrService getOrsrService() {
        return orsrService
    }

}