package com.netgrif.workflow.petrinet.domain.dataset.logic.action.runner

import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.context.RoleContext
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.delegate.RoleActionDelegate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.stereotype.Component

@Component
@SuppressWarnings("GrMethodMayBeStatic")
abstract class RoleActionsRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleActionsRunner.class)

    @Lookup("roleActionDelegate")
    abstract RoleActionDelegate getRoleActionDelegate()

    private Map<String, Object> actionsCache = new HashMap<>()
    private Map<String, Closure> actions = new HashMap<>()

    void run(Action action, RoleContext roleContext) {

        if (!actionsCache)
            actionsCache = new HashMap<>()

        log.debug("Action: $action")
        def code = getActionCode(action)

        try {
            code.init(action, roleContext)
            code()
        } catch (Exception e) {
            log.error("Action: $action.definition")
            throw e
        }
    }

    private Closure getActionCode(Action action) {
        def code
        if (actions.containsKey(action.importId)) {
            code = actions.get(action.importId)
        } else {
            code = (Closure) new GroovyShell().evaluate("{-> ${action.definition}}")
            actions.put(action.importId, code)
        }
        code.delegate = getRoleActionDelegate()
        return code
    }


}
