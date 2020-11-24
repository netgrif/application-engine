package com.netgrif.workflow.petrinet.domain.dataset.logic


class TaskChangedFieldContainer {

    private Map<String, Map<String, ChangedField>> changedFields

    TaskChangedFieldContainer() {
        this(new HashMap<String, Map<String, ChangedField>>())
    }

    TaskChangedFieldContainer(Map<String, Map<String, ChangedField>> changedFields) {
        this.changedFields = changedFields
    }

    Map<String, Map<String, ChangedField>> getChangedFields() {
        return changedFields
    }

    void add(String taskId, String fieldId, ChangedField changedField) {
        if (!changedFields.containsKey(taskId)) {
            changedFields.put(taskId, new HashMap<>())
        }

        changedFields.get(taskId).put(fieldId, changedField)
    }

    ChangedFieldContainer flatten(String rootTaskId) {
        return flatten(rootTaskId, this.changedFields, new ChangedFieldContainer())
    }

    static ChangedFieldContainer flatten(String rootTaskId, Map<String, Map<String, ChangedField>> changedFields, ChangedFieldContainer container) {
        changedFields.each { taskFields ->
            String taskIdPrefix = taskFields.key == rootTaskId ? "" : (taskFields.key + "-")
            taskFields.value.each { field ->
                container.changedFields.put(taskIdPrefix + field.value.id, field.value.attributes)
            }
        }
        return container
    }

    void mergeChanges(Map<String, Map<String, ChangedField>> newChangedFields) {
        newChangedFields.forEach({ taskId, fieldsMap ->
            if (!changedFields.containsKey(taskId)) {
                changedFields.put(taskId, new HashMap<>())
            }

            mergeChanges(changedFields.get(taskId), fieldsMap);
        })
    }

    void mergeChanges(Map<String, ChangedField> changedFields, Map<String, ChangedField> newChangedFields) {
        newChangedFields.forEach({ s, changedField ->
            if (changedFields.containsKey(s))
                changedFields.get(s).merge(changedField)
            else
                changedFields.put(s, changedField)
        })
    }

}