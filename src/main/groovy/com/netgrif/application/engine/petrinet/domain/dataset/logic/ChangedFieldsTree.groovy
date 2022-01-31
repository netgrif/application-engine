package com.netgrif.application.engine.petrinet.domain.dataset.logic

import com.netgrif.application.engine.workflow.domain.Task
import com.querydsl.core.annotations.QueryExclude
import lombok.Getter
import lombok.Setter

@QueryExclude
class ChangedFieldsTree extends CaseChangedFields {

    @Getter
    @Setter
    private String taskId

    @Getter
    @Setter
    private String transitionId

    @Getter
    @Setter
    private Map<String, CaseChangedFields> propagatedChanges = new HashMap<>()

    ChangedFieldsTree() {
        super()
    }

    ChangedFieldsTree(String caseId, String taskId, String transitionId) {
        super(caseId)
        this.taskId = taskId
        this.transitionId = transitionId
    }

    static ChangedFieldsTree createNew(String caseId, Task task) {
        return new ChangedFieldsTree(caseId, task.stringId, task.transitionId)
    }

    static ChangedFieldsTree createNew(String caseId, String taskId, String transitionId) {
        return new ChangedFieldsTree(caseId, taskId, transitionId)
    }

    void setPropagatedChanges(Map<String, CaseChangedFields> propagatedChanges) {
        this.propagatedChanges = propagatedChanges
    }

    Map<String, CaseChangedFields> getPropagatedChanges() {
        return propagatedChanges
    }

    void put(String fieldId, ChangedField changedField) {
        changedField.wasChangedOn(this.taskId, this.transitionId)
        if (!changedFields.containsKey(fieldId)) {
            changedFields.put(fieldId, changedField)
        } else {
            changedFields.get(fieldId).merge(changedField)
        }
        findInPropagated(fieldId).ifPresent() { it.merge(changedField) }
    }

    void addBehavior(String fieldId, Map<String, Set<FieldBehavior>> behavior) {
        ChangedField changedField = changedFields.get(fieldId)
        changedField.addBehavior(behavior)
        findInPropagated(fieldId).ifPresent() { it.merge(changedField) }
    }

    void addAttribute(String fieldId, String attribute, Object value) {
        ChangedField changedField = changedFields.get(fieldId)
        changedField.addAttribute(attribute, value)
        findInPropagated(fieldId).ifPresent() { it.merge(changedField) }
    }

    void mergeChangedFields(ChangedFieldsTree newChangedFields) {
        mergeChanges(this.changedFields, newChangedFields.changedFields)
        mergePropagated(newChangedFields)
    }

    void propagate(ChangedFieldsTree toPropagate) {
        addPropagated(toPropagate.caseId, toPropagate.changedFields)
        mergePropagated(toPropagate)
    }

    void addPropagated(String caseId, Map<String, ChangedField> propagatedFields) {
        if (this.caseId == caseId) {
            propagatedFields.each {
                if (this.changedFields.containsKey(it.key)) {
                    this.changedFields.get(it.key).merge(it.value)
                }
            }
        }

        if (!propagatedChanges.containsKey(caseId)) {
            propagatedChanges.put(caseId, new CaseChangedFields(caseId, new HashMap<>(propagatedFields)))
        } else {
            propagatedChanges.get(caseId).mergeChanges(propagatedFields)
        }
    }

    ChangedFieldContainer flatten() {
        return flatten(new ChangedFieldContainer())
    }

    ChangedFieldContainer flatten(ChangedFieldContainer container) {
        Map<String, ChangedField> result = new HashMap<>()
        this.propagatedChanges.each { caseFields ->
            Map<String, ChangedField> localChanges = [:]
            caseFields.value.changedFields.each { changedFields ->
                changedFields.value.changedOn.each {
                    if (it.taskId != this.taskId) {
                        substituteTaskRefFieldBehavior(changedFields.value, it.transition, it.taskId, this.transitionId)
                    }
                    if (caseFields.key == this.caseId) {
                        localChanges.put(changedFields.key, changedFields.value)
                    }
                    localChanges.put(it.taskId + "-" + changedFields.key, changedFields.value)
                }
            }
            mergeChanges(result, localChanges)
        }
        mergeChanges(result, this.changedFields)
        result.each {
            container.changedFields.put(it.key, it.value.attributes)
        }
        return container
    }

    private void mergePropagated(ChangedFieldsTree newChangedFields) {
        newChangedFields.propagatedChanges.each { caseChangedFields ->
            addPropagated(caseChangedFields.key, caseChangedFields.value.changedFields)
        }
    }

    private Optional<ChangedField> findInPropagated(String fieldId) {
        return findInPropagated(this.caseId, fieldId)
    }

    private Optional<ChangedField> findInPropagated(String caseId, String fieldId) {
        if (!propagatedChanges[caseId]) {
            return Optional.empty()
        }

        return Optional.ofNullable(propagatedChanges[caseId].changedFields[fieldId])
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
