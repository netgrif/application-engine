package com.netgrif.workflow.petrinet.domain.dataset.logic


import com.querydsl.core.annotations.QueryExclude

@QueryExclude
class ChangedFieldsTree {

    private TaskChangedFields root

    ChangedFieldsTree(TaskChangedFields root) {
        this.root = root
    }

    TaskChangedFields getRoot() {
        return root
    }

    static ChangedFieldsTree createNew(String taskId) {
        return new ChangedFieldsTree(new TaskChangedFields(taskId))
    }

    ChangedFieldContainer flatten() {
        return flatten(new ChangedFieldContainer())
    }

    ChangedFieldContainer flatten(ChangedFieldContainer existing) {
        Map<String, ChangedField> fields = collectChangedFields()
        fields.each {
            existing.changedFields.put(it.key, it.value.attributes)
        }
        return existing
    }

    void mergeChangesOnTaskTree(ChangedFieldsTree newChangedFields) {
        if (root.getTaskId() == newChangedFields.getRoot().getTaskId()) {
            mergeChanges(root.getChangedFields(), newChangedFields.getRoot().getChangedFields())
            newChangedFields.root.getChildren().each {
                root.addChild(it.value)
            }

        } else {
            append(newChangedFields.getRoot())
        }
    }

    void append(TaskChangedFields changedFields) {
        this.root.addChild(changedFields)
    }

    private void mergeChanges(Map<String, ChangedField> changedFields, Map<String, ChangedField> newChangedFields) {
        newChangedFields.forEach({ s, changedField ->
            if (changedFields.containsKey(s))
                changedFields.get(s).merge(changedField)
            else
                changedFields.put(s, changedField)
        })
    }

    private Map<String, ChangedField> collectChangedFields() {
        Map<String, ChangedField> result = new HashMap<>()
        collectChangedFields(root, result)
        return result
    }

    private void collectChangedFields(TaskChangedFields taskChangedFields, Map<String, ChangedField> result) {
        taskChangedFields.changedFields.each { changedField ->
            String complexId = taskChangedFields.taskId == this.root.taskId ?
                    changedField.value.id : taskChangedFields.taskId + "-" + changedField.value.id
            if (result.containsKey(complexId)) {
                result.get(complexId).merge(changedField.value)
            } else {
                result.put(complexId, changedField.value)
            }
        }

        taskChangedFields.children.each { taskFields ->
            collectChangedFields(taskFields.value, result)
        }

    }
}
