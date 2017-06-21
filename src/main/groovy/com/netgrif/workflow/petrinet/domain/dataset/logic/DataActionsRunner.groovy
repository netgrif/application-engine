package com.netgrif.workflow.petrinet.domain.dataset.logic

import com.netgrif.workflow.petrinet.domain.Transition
import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.workflow.domain.Case

class DataActionsRunner {

    private Case useCase

    DataActionsRunner(Case useCase) {
        this.useCase = useCase
    }

    void run(String script) {
        Binding binding = new Binding()
        bindVariables(script, binding)
        def shell = new GroovyShell(binding)

        def code = (Closure) shell.evaluate("{->${getExpresion(script)}}")
        code.delegate = new ActionDelegate()
        code()
    }

    void bindVariables(String script, Binding binding) {
        String[] vars = getVariables(script)
        vars.each { binding.setVariable(getVarName(it), getVarValue(it)) }
    }

    static String[] getVariables(String script) {
        return script.split(";")[0].trim().split(",")
    }

    static String getExpresion(String script) {
        return script.split(";")[1].trim()
    }

    static String getVarName(String variable) {
        return variable.split(":")[0].trim()
    }

    Object getVarValue(String variable) {
        String varDef = variable.split(":")[1].trim()
        String[] varParts = varDef.split("\\.")
        String clazz = varParts[0].trim()
        String objectId = varParts[1].trim()

        if (clazz.equalsIgnoreCase("f"))
            return getFieldFromVariable(objectId)

        else if (clazz.equalsIgnoreCase("t"))
            return getTransitionFromVariable(objectId)

        else throw new IllegalVariableTypeException(clazz)
    }

    Field getFieldFromVariable(String objectId) {
        Field field = useCase.petriNet.dataSet.get(objectId)
        field.value = useCase.dataSetValues.get(objectId)
        return field
    }

    Transition getTransitionFromVariable(String objectId) {
        return useCase.petriNet.transitions.get(objectId)
    }


}
