package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import com.netgrif.workflow.importer.Importer
import com.netgrif.workflow.petrinet.domain.Transition
import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField
import com.netgrif.workflow.petrinet.domain.dataset.logic.IllegalVariableTypeException
import com.netgrif.workflow.workflow.domain.Case

class FieldActionsRunner {

    FieldActionsRunner() {
    }

    static ChangedField run(String script, Case useCase) {
        Binding binding = new Binding()
        bindVariables(script, binding, useCase)
        def shell = new GroovyShell(binding)

        def code = (Closure) shell.evaluate("{->${getExpression(script)}}")
        code.delegate = new ActionDelegate(useCase)
        code()
        return ((ActionDelegate)code.delegate).changedField
    }

    static void bindVariables(String script, Binding binding, Case useCase) {
        String[] vars = getVariables(script)
        vars.each { binding.setVariable(getVarName(it), getVarValue(it, useCase)) }
    }

    static String[] getVariables(String script) {
        return script.split(";")[0].trim().split(",")
    }

    static String getExpression(String script) {
        return script.split(";", 2)[1].trim()
    }

    static String getVarName(String variable) {
        return variable.split(":")[0].trim()
    }

    static Object getVarValue(String variable, Case useCase) {
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

    static Field getFieldFromVariable(String objectId, Case useCase) {
        Field field = useCase.petriNet.dataSet.get(objectId)
        field.value = useCase.dataSet.get(objectId).value
        return field
    }

    static Transition getTransitionFromVariable(String objectId, Case useCase) {
        return useCase.petriNet.transitions.get(objectId)
    }


}
