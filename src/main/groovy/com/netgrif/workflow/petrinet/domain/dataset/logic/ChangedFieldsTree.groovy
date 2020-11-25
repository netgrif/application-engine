package com.netgrif.workflow.petrinet.domain.dataset.logic

import com.netgrif.workflow.workflow.domain.Task
import com.querydsl.core.annotations.QueryExclude

@QueryExclude
class ChangedFieldsTree extends TaskChangedFields {

    /**
     * caseId -> [taskId -> changedFields]
     */
    private Map<String, Map<String, TaskChangedFields>> propagatedChanges = new HashMap<>()

    ChangedFieldsTree(String caseId, String taskId, String transitionId) {
        super(caseId, taskId, transitionId)
    }

    static ChangedFieldsTree createNew(String caseId, Task task) {
        return new ChangedFieldsTree(caseId, task.stringId, task.transitionId)
    }

    static ChangedFieldsTree createNew(String caseId, String taskId, String transitionId) {
        return new ChangedFieldsTree(caseId, taskId, transitionId)
    }

    void setPropagatedChanges(Map<String, Map<String, TaskChangedFields>> propagatedChanges) {
        this.propagatedChanges = propagatedChanges
    }

    Map<String, Map<String, TaskChangedFields>> getPropagatedChanges() {
        return propagatedChanges
    }

    void put(String fieldId, ChangedField changedField) {
        if (!changedFields.containsKey(fieldId)) {
            changedFields.put(fieldId, changedField)
        } else {
            changedFields.get(fieldId).merge(changedField)
        }
        findInPropagated(fieldId).each {it.merge(changedField) }
    }

    void addBehavior(String fieldId, Map<String, Set<FieldBehavior>> behavior) {
        ChangedField changedField = changedFields.get(fieldId)
        changedField.addBehavior(behavior)
        findInPropagated(fieldId).each { it.merge(changedField) }
    }

    void addAttribute(String fieldId, String attribute, Object value) {
        ChangedField changedField = changedFields.get(fieldId)
        changedField.addAttribute(attribute, value)
        findInPropagated(fieldId).each { it.merge(changedField) }
    }

    void mergeChangedFields(ChangedFieldsTree newChangedFields) {
        mergeChanges(this.changedFields, newChangedFields.changedFields)
        mergePropagated(newChangedFields)
    }

    void propagate(ChangedFieldsTree toPropagate) {
        addPropagated(toPropagate.caseId, toPropagate.taskId, toPropagate.transitionId, toPropagate.changedFields)
        mergePropagated(toPropagate)
    }

    void addPropagated(String caseId, Task task, Map<String, ChangedField> propagatedFields) {
       addPropagated(caseId, task.stringId, task.transitionId, propagatedFields)
    }

    void addPropagated(String caseId, String taskId, String transitionId, Map<String, ChangedField> propagatedFields) {
        if (this.caseId == caseId) {
            propagatedFields.each {
                if (this.changedFields.containsKey(it.key)) {
                    this.changedFields.get(it.key).merge(it.value)
                }
            }
        }

        if (!propagatedChanges.containsKey(caseId)) {
            propagatedChanges.put(caseId, new HashMap<>())
        }

        Map<String, TaskChangedFields> taskChangedFields = propagatedChanges.get(caseId)
        if (!taskChangedFields.containsKey(taskId)) {
            taskChangedFields.put(taskId, new TaskChangedFields(caseId, taskId, transitionId, new HashMap<>(propagatedFields)))
        } else {
            taskChangedFields.get(taskId).mergeChanges(propagatedFields)
        }
    }

    ChangedFieldContainer flatten() {
        return flatten(new ChangedFieldContainer())
    }

    ChangedFieldContainer flatten(ChangedFieldContainer container) {
        Map<String, ChangedField> result = new HashMap<>()
        this.propagatedChanges.each { caseFields ->
            caseFields.value.each { taskFields ->
                String prefix = caseFields.key == this.caseId ? "" : taskFields.key + "-"
                Map<String, ChangedField> localChanges = taskFields.value.changedFields.collectEntries {
                    if (taskFields.value.taskId != this.taskId) {
                        substituteTaskRefFieldBehavior(it.value, taskFields.value.transitionId, taskFields.value.taskId, this.transitionId)
                    }
                    return [(prefix + it.key): (it.value)]
                } as Map<String, ChangedField>
                mergeChanges(result, localChanges)
            }
        }
        mergeChanges(result, this.changedFields)
        result.each {
            container.changedFields.put(it.key, it.value.attributes)
        }
        return container
    }

    private void mergePropagated(ChangedFieldsTree newChangedFields) {
        newChangedFields.propagatedChanges.each { caseFields ->
            caseFields.value.each {taskFields ->
                taskFields.value.each {
                    addPropagated(caseFields.key, taskFields.key, taskFields.value.transitionId, taskFields.value.changedFields)
                }
            }
        }
    }

    private List<ChangedField> findInPropagated(String fieldId) {
        return findInPropagated(this.caseId, fieldId)
    }

    private List<ChangedField> findInPropagated(String caseId, String fieldId) {
        List<ChangedField> occurrences = []
        if (!propagatedChanges[caseId]) {
            return occurrences
        }

        propagatedChanges[caseId].each {
            ChangedField occurrence = it.value.changedFields[fieldId]
            !occurrence ?: occurrences.add(occurrence)
        }
        return occurrences
    }

    private void substituteTaskRefFieldBehavior(ChangedField change, String referencedTaskTrans, String referencedTaskStringId, String refereeTransId) {
        substituteTaskRefFieldBehavior(change.getAttributes(), referencedTaskTrans, referencedTaskStringId, refereeTransId)
    }

    private Map<String, Object> substituteTaskRefFieldBehavior(Map<String, Object> change, String referencedTaskTrans, String referencedTaskStringId, String refereeTransId) {
        if (change.containsKey("behavior")) {
            Map<String, Object> newBehavior = new HashMap<>()
            ((Map<String, Object>) change.get("behavior")).forEach({ transId, behavior ->
                String behaviorChangedOnTrans = transId == referencedTaskTrans ?
                        refereeTransId : referencedTaskStringId + "-" + transId
                newBehavior.put(behaviorChangedOnTrans, behavior)
            });
            change.put("behavior", newBehavior)
        }
        return change
    }
}
