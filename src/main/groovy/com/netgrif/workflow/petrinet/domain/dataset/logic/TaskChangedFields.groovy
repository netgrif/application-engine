package com.netgrif.workflow.petrinet.domain.dataset.logic


class TaskChangedFields {

    private String taskId
    private Map<String, ChangedField> changedFields = new HashMap<>()
    private Map<String, TaskChangedFields> children = new HashMap<>()

    TaskChangedFields(String taskId) {
        this.taskId = taskId
    }

    TaskChangedFields(String taskId, Map<String, ChangedField> changedFields) {
        this.taskId = taskId
        this.changedFields = changedFields
    }

    void put(String fieldId, ChangedField changedField) {
        if (!changedFields.containsKey(fieldId)) {
            changedFields.put(fieldId, changedField)
        } else {
            changedFields.get(fieldId).merge(changedField)
        }
    }

    void addBehavior(String fieldId, Map<String, Set<FieldBehavior>> behavior) {
        changedFields.get(fieldId).addBehavior(behavior)
    }

    void addChild(TaskChangedFields changedFields) {
        if (!children.containsKey(changedFields.taskId)) {
            children.put(changedFields.taskId, changedFields)
        } else {
            children.get(changedFields.taskId).merge(changedFields)
        }
    }

    void merge(TaskChangedFields taskChangedFields) {
        changedFields.putAll(taskChangedFields.changedFields) // TODO NAE-1109
        children.putAll(taskChangedFields.children)
    }

    String getTaskId() {
        return taskId
    }

    Map<String, ChangedField> getChangedFields() {
        return changedFields
    }

    Map<String, TaskChangedFields> getChildren() {
        return children
    }
}
