package com.netgrif.workflow.admin

import com.netgrif.workflow.petrinet.domain.dataset.logic.action.ActionDelegate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.stereotype.Component

@Component
@SuppressWarnings("GrMethodMayBeStatic")
abstract class AdminConsoleRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminConsoleRunner.class)

    @Lookup("actionDelegate")
    abstract ActionDelegate getActionDelegate()

    String run(String action) {
        log.debug("Action: $action")
        def code = getActionCode(action)
        return code().toString()
    }

    private Closure getActionCode(String action) {
        def code = (Closure) new GroovyShell(this.class.getClassLoader()).evaluate("{-> ${action}}")
        code.delegate = getActionDelegate()
        return code
    }

}
