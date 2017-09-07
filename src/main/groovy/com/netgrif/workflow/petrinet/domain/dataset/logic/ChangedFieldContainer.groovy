package com.netgrif.workflow.petrinet.domain.dataset.logic

class ChangedFieldContainer {

    private Map<String,Map<String,Object>> changedFields

    ChangedFieldContainer() {
        changedFields = new HashMap<>()
    }

    void putAll(Map<String, ChangedField> changed){
        changed.each { key, field -> changedFields.put(field.id,field.attributes)}
    }

    Map<String, Map<String, Object>> getChangedFields() {
        return changedFields
    }
}
