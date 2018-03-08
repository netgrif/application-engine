package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import com.netgrif.workflow.business.IPostalCodeService
import com.netgrif.workflow.business.orsr.IOrsrService
import com.netgrif.workflow.importer.service.FieldFactory
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.Transition
import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField
import com.netgrif.workflow.petrinet.domain.dataset.logic.IllegalVariableTypeException
import com.netgrif.workflow.workflow.domain.Case
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@SuppressWarnings("GrMethodMayBeStatic")
class FieldActionsRunner {

    private static final Logger log = LoggerFactory.getLogger(FieldActionsRunner.class)

    @Autowired
    private IOrsrService orsrService

    @Autowired
    private IPostalCodeService postalCodeService

    @Autowired
    private FieldFactory fieldFactory

    private Map<String, Object> actionsCache = new HashMap<>()

    FieldActionsRunner() {
        actionsCache = new HashMap<>()
    }

    ChangedField run(String script, Case useCase) {
        if (!actionsCache)
            actionsCache = new HashMap<>()

        Binding binding = new Binding()
        bindVariables(script, binding, useCase)
        def shell = new GroovyShell(binding)

        log.debug("Action: $script")
        def code = (Closure) shell.evaluate("{->${getExpression(script)}}")
        code.delegate = new ActionDelegate(useCase, this)
        code()
        return ((ActionDelegate) code.delegate).changedField
    }

    void bindVariables(String script, Binding binding, Case useCase) {
        String[] vars = getVariables(script)
        if (vars.length == 1.intValue() && !vars[0].contains(":"))
            return

        vars.each { binding.setVariable(getVarName(it), getVarValue(it, useCase)) }
    }

    String[] getVariables(String script) {
        return script.split(";")[0].trim().split(",")
    }

    String getExpression(String script) {
        return script.split(";", 2)[1].trim()
    }

    String getVarName(String variable) {
        return variable.split(":")[0].trim()
    }

    Object getVarValue(String variable, Case useCase) {
        String varDef = variable.split(":")[1].trim()
        String[] varParts = varDef.split("\\.")
        String clazz = varParts[0].trim()
        String objectId = varParts[1].trim()

        if (clazz.equalsIgnoreCase(Importer.FIELD_KEYWORD))
            return getFieldFromVariable(objectId, useCase)

        else if (clazz.equalsIgnoreCase(Importer.TRANSITION_KEYWORD))
            return getTransitionFromVariable(objectId, useCase)

        else throw new IllegalVariableTypeException(clazz)
    }

    Field getFieldFromVariable(String objectId, Case useCase) {
        return fieldFactory.buildFieldWithoutValidation(useCase, objectId)
    }

    Transition getTransitionFromVariable(String objectId, Case useCase) {
        return useCase.petriNet.transitions.get(objectId)
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