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

    void run(String action) {
//        if (!actionsCache)
//            actionsCache = new HashMap<>()
//
//        log.debug("Action: $action")
//        def code = getActionCode(action)
//
//        try {
////            code.init(action)
//            code()
//        } catch (Exception e) {
//            log.error("Action: $action")
//            throw e
//        }
    }

    private Closure getActionCode(String action) {
        def code
//        if (actions.containsKey(action.importId)) {
//            code = actions.get(action.importId)
//        } else {
//            code = (Closure) new GroovyShell().evaluate("{-> ${action}}")
//            actions.put(action.importId, code)
//        }
        code.delegate = getActionDeleget()
        return code
    }


}
