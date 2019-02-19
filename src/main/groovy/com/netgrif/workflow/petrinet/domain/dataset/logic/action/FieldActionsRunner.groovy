package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import com.netgrif.workflow.business.IPostalCodeService
import com.netgrif.workflow.business.orsr.IOrsrService
import com.netgrif.workflow.importer.service.FieldFactory
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField
import com.netgrif.workflow.workflow.domain.Case
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

    private Map<String, Object> actionsCache = new HashMap<>()
    private Map<String, Closure> actions = new HashMap<>()

    FieldActionsRunner() {
        actionsCache = new HashMap<>()
    }

    Map<String, ChangedField> run(Action action, Case useCase) {
        if (!actionsCache)
            actionsCache = new HashMap<>()

        log.debug("Action: $action")
        def code = getActionCode(action)

        try {
            code.init(action, useCase, this)
            code()
        } catch (Exception e) {
            log.error("Action: $action.definition")
            throw e
        }
        return ((ActionDelegate) code.delegate).changedFields
    }

    Closure getActionCode(Action action) {
        def code
        if (actions.containsKey(action.importId)) {
            code = actions.get(action.importId)
        } else {
            code = (Closure) new GroovyShell().evaluate("{-> ${action.definition}}")
            actions.put(action.importId, code)
        }
        code.delegate = getActionDeleget()
        return code
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