package com.netgrif.workflow.admin

import com.netgrif.workflow.event.IGroovyShellFactory
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.ActionDelegate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.stereotype.Component

@Component
@SuppressWarnings("GrMethodMayBeStatic")
abstract class AdminConsoleRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminConsoleRunner.class)

    @Lookup("actionDelegate")
    abstract ActionDelegate getActionDelegate()

    @Autowired
    private IGroovyShellFactory shellFactory

    String run(String action) {
        log.debug("Action: $action")
        def code = getActionCode(action)
        return code().toString()
    }

    private Closure getActionCode(String action) {
        def code = (Closure) this.shellFactory.getGroovyShell().evaluate("{-> ${action}}")
        code.delegate = getActionDelegate()
        return code
    }

}
