package com.netgrif.workflow.petrinet.domain.dataset.logic.action.runner


import com.netgrif.workflow.configuration.properties.ActionsProperties
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.context.RoleContext
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.delegate.RoleActionDelegate
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
    private ImportCustomizer importCustomizer
    private CompilerConfiguration configuration

    @Autowired
    RoleActionsRunner(ActionsProperties actionsProperties) {
        importCustomizer = new ImportCustomizer()
        importCustomizer.addImports(actionsProperties.imports as String[])
        importCustomizer.addStarImports(actionsProperties.starImports as String[])
        importCustomizer.addStaticStars(actionsProperties.staticStarImports as String[])
        configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers(importCustomizer)
    }

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
            code = (Closure) new GroovyShell(configuration).evaluate("{-> ${action.definition}}")
            actions.put(action.importId, code)
        }
        return code.rehydrate(getRoleActionDelegate(), code.owner, code.thisObject)
    }
}
