package com.netgrif.workflow.petrinet.domain.dataset.logic.logic

import com.netgrif.workflow.petrinet.domain.dataset.FileField
import com.netgrif.workflow.workflow.domain.Case

import java.lang.reflect.Method


class FileFieldLogic extends FieldLogic{

    private static final String GENERATION_METHODS_PACKAGE = "com.netgrif.workflow.petrinet.domain.dataset.logic.logic."

    protected FileField field

    FileFieldLogic(Case useCase, FileField field) {
        super(useCase)
        this.field = field
    }

    public List<Object> executeLogic(){
        if(field.logic == null) return null
        if(field.logic.isEmpty()) return null

        List<Object> results = new ArrayList<>()
        field.logic.each {member ->
            if(!shouldExecute(member)) return
            if(member.contains("."))
                results.add(callMethod(member))
        }
        if(results.isEmpty())
            results.add(new File(field.getFilePath((String)useCase.dataSet.get(field.objectId).value)))

        return results
    }

    private Object callMethod(String logicMember){
        try {
            logicMember = logicMember.replace("always","").trim()
            String[] parts = logicMember.split("\\.")
            Class clazz = Class.forName(GENERATION_METHODS_PACKAGE + parts[0])

            Object t = clazz.newInstance(useCase, field)
            Method m = clazz.getMethod(parts[1])
            return m.invoke(t)

        } catch (Exception e){
            e.printStackTrace()
            return null
        }
    }

    private boolean shouldExecute(String logic){
        if(logic.contains("always")) return true
        return useCase.dataSet.get(field.objectId).value == field.getDefaultValue()
    }
}
