package com.netgrif.workflow.petrinet.domain.dataset.logic

import com.querydsl.core.annotations.QueryExclude

@QueryExclude
class ChangedFieldsTree extends TaskChangedFields {

    private Map<String, TaskChangedFields> propagatedChanges = new HashMap<>()

    ChangedFieldsTree(String taskId) {
        super(taskId)
    }

    static ChangedFieldsTree createNew(String taskId) {
        return new ChangedFieldsTree(taskId)
    }

    void put(String fieldId, ChangedField changedField) {
        if (!changedFields.containsKey(fieldId)) {
            changedFields.put(fieldId, changedField)
        } else {
            changedFields.get(fieldId).merge(changedField)
        }
        findInPropagated(fieldId).ifPresent {it.merge(changedField) }
    }

    void addBehavior(String fieldId, Map<String, Set<FieldBehavior>> behavior) {
        ChangedField changedField = changedFields.get(fieldId)
        changedField.addBehavior(behavior)
        findInPropagated(fieldId).ifPresent { it.merge(changedField) }
    }

    void addAttribute(String fieldId, String attribute, Object value) {
        ChangedField changedField = changedFields.get(fieldId)
        changedField.addAttribute(attribute, value)
        findInPropagated(fieldId).ifPresent { it.merge(changedField) }
    }

    void mergeChangedFields(ChangedFieldsTree newChangedFields) {
        mergeChanges(this.changedFields, newChangedFields.changedFields)
        newChangedFields.propagatedChanges.each {
            addPropagated(it.key, it.value.changedFields)
        }
    }

    void addPropagated(String taskId, Map<String, ChangedField> propagatedFields) {
        if (this.taskId == taskId) {
            propagatedFields.each {
                if (this.changedFields.containsKey(it.key)) {
                    this.changedFields.get(it.key).merge(it.value)
                }
            }
        }

        if (!propagatedChanges.containsKey(taskId)) {
            propagatedChanges.put(taskId, new TaskChangedFields(taskId, new HashMap<>(propagatedFields)))
        } else {
            propagatedChanges.get(taskId).mergeChanges(propagatedFields)
        }
    }

    Map<String, Map<String, ChangedField>> toMap() {
        Map<String, Map<String, ChangedField>> result = new HashMap<>()
        this.propagatedChanges.each {
            if (!result.containsKey(it.key)) {
                result.put(it.key, new HashMap<String, ChangedField>())
            }
            mergeChanges(result.get(it.key), it.value.changedFields)
        }

        if (!result.containsKey(this.taskId)) {
            result.put(this.taskId, new HashMap<String, ChangedField>())
        }
        mergeChanges(result.get(this.taskId), this.changedFields)

        return result
    }

    Map<String, TaskChangedFields> getPropagatedChanges() {
        return propagatedChanges
    }

    private Optional<ChangedField> findInPropagated(String fieldId) {
        if (propagatedChanges[taskId] && propagatedChanges[taskId].changedFields[fieldId]) {
            return Optional.of(propagatedChanges[taskId].changedFields[fieldId])
        } else {
            return Optional.empty()
        }
    }
}
