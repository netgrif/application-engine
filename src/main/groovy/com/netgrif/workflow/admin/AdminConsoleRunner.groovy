package com.netgrif.workflow.admin


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.stereotype.Component

@Component
@SuppressWarnings("GrMethodMayBeStatic")
abstract class AdminConsoleRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminConsoleRunner.class)


    @Lookup("actionDelegate")
    abstract AdminConslole getActionDeleget()

    private Map<String, Object> actionsCache = new HashMap<>()
    private Map<String, Closure> actions = new HashMap<>()

    Object run(String action) {

        log.debug("Action: $action")
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);
//        def code = getActionCode(action)

        Object result = shell.evaluate(action);
        return result
    }

    private Closure getActionCode(String action) {
        def code = (Closure) new GroovyShell().evaluate("{-> ${action}}")
         code.delegate = getActionDeleget()
        return code
    }


}
