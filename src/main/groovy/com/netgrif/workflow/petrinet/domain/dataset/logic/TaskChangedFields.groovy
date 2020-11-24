package com.netgrif.workflow.petrinet.domain.dataset.logic


class TaskChangedFields {

    protected String taskId
    protected Map<String, ChangedField> changedFields = new HashMap<>()

    TaskChangedFields(String taskId) {
        this.taskId = taskId
    }

    TaskChangedFields(String taskId, Map<String, ChangedField> changedFields) {
        this.taskId = taskId
        this.changedFields = changedFields
    }

    void mergeChanges(Map<String, ChangedField> newChangedFields) {
        mergeChanges(this.changedFields, newChangedFields)
    }

    void mergeChanges(Map<String, ChangedField> changedFields, Map<String, ChangedField> newChangedFields) {
        newChangedFields.forEach({ s, changedField ->
            if (changedFields.containsKey(s))
                changedFields.get(s).merge(changedField)
            else
                changedFields.put(s, changedField)
        })
    }

    String getTaskId() {
        return taskId
    }

    Map<String, ChangedField> getChangedFields() {
        return changedFields
    }
}
