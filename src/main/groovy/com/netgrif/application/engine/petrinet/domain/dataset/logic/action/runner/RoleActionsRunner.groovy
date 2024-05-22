package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner


import com.netgrif.application.engine.event.IGroovyShellFactory
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.context.RoleContext
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.delegate.RoleActionDelegate
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.stereotype.Component

@Slf4j
@Component
@SuppressWarnings("GrMethodMayBeStatic")
abstract class RoleActionsRunner {

    @Lookup("roleActionDelegate")
    abstract RoleActionDelegate getRoleActionDelegate()

    @Autowired
    private IGroovyShellFactory shellFactory

    private Map<String, Object> actionsCache = new HashMap<>()
    private Map<String, Closure> actions = new HashMap<>()

    void run(Action action, RoleContext roleContext, Map<String, String> params) {
        if (!actionsCache)
            actionsCache = new HashMap<>()

        log.debug("Action: $action")
        def code = getActionCode(action)
        try {
            code.init(action, roleContext, params)
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
            code = (Closure) this.shellFactory.getGroovyShell().evaluate("{-> ${action.definition}}")
            actions.put(action.importId, code)
        }
        return code.rehydrate(getRoleActionDelegate(), code.owner, code.thisObject)
    }
}
