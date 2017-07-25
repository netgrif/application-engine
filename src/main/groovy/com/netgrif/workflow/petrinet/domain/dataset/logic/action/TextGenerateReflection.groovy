package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import com.netgrif.workflow.petrinet.domain.dataset.FileField
import com.netgrif.workflow.petrinet.domain.dataset.TextField
import com.netgrif.workflow.workflow.domain.Case

import java.lang.reflect.Method

class TextGenerateReflection {

    private static final String GENERATION_METHODS_PACKAGE = "com.netgrif.workflow.petrinet.domain.dataset.logic.action."

    private Case useCase
    private TextField field
    private Boolean alwaysGenerate

    TextGenerateReflection(Case useCase, TextField field, Boolean always) {
        this.useCase = useCase
        this.field = field
        this.alwaysGenerate = always
    }

    @Deprecated
    List<Object> executeLogic() {
        if (field.logic == null) return null
        if (field.logic.isEmpty()) return null

        List<Object> results = new ArrayList<>()
        field.logic.each { member ->
            if (!shouldExecute(member)) return
            if (member.contains("."))
                results.add(callMethod(member))
        }
        if (results.isEmpty())
            results.add(new File(field.getFilePath((String) useCase.dataSet.get(field.objectId).value)))

        return results
    }

    Object callMethod(String calledMethod) {
        try {
            if (!alwaysGenerate && useCase.dataSet.get(field.objectId).value != field.getDefaultValue())
                return useCase.dataSet.get(field.objectId).value as String

            String[] parts = calledMethod.split("\\.")
            Class clazz = Class.forName(GENERATION_METHODS_PACKAGE + parts[0])

            Object t = clazz.newInstance(useCase, field)
            Method m = clazz.getMethod(parts[1])
            return m.invoke(t)

        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }
}