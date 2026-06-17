package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic;

import com.netgrif.application.engine.objects.workflow.domain.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Setter
@Getter
public class ChangedFieldsTree extends CaseChangedFields {

    private String taskId;

    private String transitionId;

    private Map<String, CaseChangedFields> propagatedChanges = new HashMap<>();

    public ChangedFieldsTree() {
        super();
    }

    public ChangedFieldsTree(String caseId, String taskId, String transitionId) {
        super(caseId);
        this.taskId = taskId;
        this.transitionId = transitionId;
    }

    public static ChangedFieldsTree createNew(String caseId, Task task) {
        return new ChangedFieldsTree(caseId, task.getStringId(), task.getTransitionId());
    }

    public static ChangedFieldsTree createNew(String caseId, String taskId, String transitionId) {
        return new ChangedFieldsTree(caseId, taskId, transitionId);
    }

    public void put(String fieldId, final ChangedField changedField) {
        changedField.wasChangedOn(this.taskId, this.transitionId);
        if (!getChangedFields().containsKey(fieldId)) {
            getChangedFields().put(fieldId, changedField);
        } else {
            getChangedFields().get(fieldId).merge(changedField);
        }
        findInPropagated(fieldId).ifPresent(it -> it.merge(changedField));
    }

    public void addBehavior(String fieldId, Map<String, Set<FieldBehavior>> behavior) {
        final ChangedField changedField = getChangedFields().get(fieldId);
        changedField.addBehavior(behavior);
        findInPropagated(fieldId).ifPresent(it -> it.merge(changedField));
    }

    public void addAttribute(String fieldId, String attribute, Object value) {
        final ChangedField changedField = getChangedFields().get(fieldId);
        changedField.addAttribute(attribute, value);
        findInPropagated(fieldId).ifPresent(it -> it.merge(changedField));
    }

    public void mergeChangedFields(ChangedFieldsTree newChangedFields) {
        mergeChanges(this.getChangedFields(), newChangedFields.getChangedFields());
        mergePropagated(newChangedFields);
    }

    public void propagate(ChangedFieldsTree toPropagate) {
        addPropagated(toPropagate.getCaseId(), toPropagate.getChangedFields());
        mergePropagated(toPropagate);
    }

    public void addPropagated(String caseId, Map<String, ChangedField> propagatedFields) {
        if (this.getCaseId().equals(caseId)) {
            propagatedFields.forEach((key, value) -> {
                if (ChangedFieldsTree.this.getChangedFields().containsKey(key)) {
                    ChangedFieldsTree.this.getChangedFields().get(key).merge(value);
                }
            });
        }
        if (!propagatedChanges.containsKey(caseId)) {
            propagatedChanges.put(caseId, new CaseChangedFields(caseId, new HashMap<>(propagatedFields)));
        } else {
            propagatedChanges.get(caseId).mergeChanges(propagatedFields);
        }

    }

    public ChangedFieldContainer flatten() {
        return flatten(new ChangedFieldContainer());
    }

    public ChangedFieldContainer flatten(final ChangedFieldContainer container) {
        final Map<String, ChangedField> result = new HashMap<String, ChangedField>();
        this.propagatedChanges.forEach((key, value) -> {
            final Map<String, ChangedField> localChanges = new LinkedHashMap<String, ChangedField>();
            value.getChangedFields().forEach((key1, value1) -> value1.getChangedOn().forEach(it -> {
                if (!it.taskId.equals(ChangedFieldsTree.this.taskId)) {
                    substituteTaskRefFieldBehavior(value1, it.transition, it.taskId, ChangedFieldsTree.this.transitionId);
                }
                if (key.equals(ChangedFieldsTree.this.getCaseId())) {
                    localChanges.put(key1, value1);
                }
                localChanges.put(it.taskId + "-" + key1, value1);
            }));
            mergeChanges(result, localChanges);
        });

        mergeChanges(result, this.getChangedFields());
        result.forEach((key, value) -> container.getChangedFields().put(key, value.getAttributes()));
        return container;
    }

    private void mergePropagated(ChangedFieldsTree newChangedFields) {
        newChangedFields.getPropagatedChanges().forEach((key, value) -> addPropagated(key, value.getChangedFields()));
    }

    private Optional<ChangedField> findInPropagated(String fieldId) {
        return findInPropagated(this.getCaseId(), fieldId);
    }

    private Optional<ChangedField> findInPropagated(String caseId, String fieldId) {
        if (!propagatedChanges.containsKey(caseId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(propagatedChanges.get(caseId).changedFields.get(fieldId));
    }

    private void substituteTaskRefFieldBehavior(ChangedField change, String referencedTaskTrans, String referencedTaskStringId, String refereeTransId) {
        substituteTaskRefFieldBehavior(change.getAttributes(), referencedTaskTrans, referencedTaskStringId, refereeTransId);
    }

    private Map<String, Object> substituteTaskRefFieldBehavior(Map<String, Object> change, final String referencedTaskTrans, final String referencedTaskStringId, final String refereeTransId) {
        if (change.containsKey("behavior")) {
            final Map<String, Object> newBehavior = new HashMap<String, Object>();
            ((Map<String, Object>) change.get("behavior")).forEach((transId, behavior) -> {
                String behaviorChangedOnTrans = transId.equals(referencedTaskTrans) ? refereeTransId : referencedTaskStringId + "-" + transId;
                newBehavior.put(behaviorChangedOnTrans, behavior);
            });
            change.put("behavior", newBehavior);
        }
        return change;
    }
}
