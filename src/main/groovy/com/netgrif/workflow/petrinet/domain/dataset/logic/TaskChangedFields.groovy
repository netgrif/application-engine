package com.netgrif.workflow.petrinet.domain.dataset.logic

import com.netgrif.workflow.workflow.domain.Task


class TaskChangedFields {

    protected String taskId
    protected String caseId
    protected String transitionId
    protected Map<String, ChangedField> changedFields = new HashMap<>()

    TaskChangedFields(String caseId, Task task) {
        this(caseId, task.stringId, task.transitionId)

    }

    TaskChangedFields(String caseId, String taskId, String transitionId) {
        this.taskId = taskId
        this.transitionId = transitionId
        this.caseId = caseId
    }

    TaskChangedFields(String caseId, String taskId, String transitionId, Map<String, ChangedField> changedFields) {
        this(caseId, taskId, transitionId)
        this.changedFields = changedFields
    }

    void mergeChanges(Map<String, ChangedField> newChangedFields) {
        mergeChanges(this.changedFields, newChangedFields)
    }

    void mergeChanges(Map<String, ChangedField> changedFields, Map<String, ChangedField> newChangedFields) {
        newChangedFields.forEach({ fieldId, changedField ->
            if (changedFields.containsKey(fieldId)) {
                changedFields.get(fieldId).merge(changedField)

            } else {
                changedFields.put(fieldId, changedField)
            }
        })
    }

    String getTaskId() {
        return taskId
    }

    String getCaseId() {
        return caseId
    }

    Map<String, ChangedField> getChangedFields() {
        return changedFields
    }
}
