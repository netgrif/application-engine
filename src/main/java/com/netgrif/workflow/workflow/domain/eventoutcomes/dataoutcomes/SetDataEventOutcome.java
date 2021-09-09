package com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.workflow.petrinet.domain.dataset.logic.CaseChangedFields;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class SetDataEventOutcome extends TaskEventOutcome {

    private Map<String, ChangedField> changedFields = new HashMap<>();

    private Map<String, CaseChangedFields> propagatedChanges = new HashMap<>();

    public SetDataEventOutcome(Case aCase, Task task) {
        super(aCase, task);
    }

    public void addChangedField(String fieldId, ChangedField field) {
        field.wasChangedOn(this.getTask() == null ? "all_data" : this.getTask().getStringId(),
                this.getTask() == null ? "all_data_transition" : this.getTask().getTransitionId());
        if (!changedFields.containsKey(fieldId)) {
            changedFields.put(fieldId, field);
        } else {
            changedFields.get(fieldId).merge(field);
        }
        findInPropagated(fieldId).ifPresent(propagatedField -> propagatedField.merge(field));
    }

    public void addPropagated(String caseId, Map<String, ChangedField> propagatedFields) {
        if (this.getACase().getStringId().equals(caseId)) {
            propagatedFields.forEach((id, field) -> {
                if (this.changedFields.containsKey(id)) {
                    this.changedFields.get(id).merge(field);
                }
            });
        }

        if (!this.propagatedChanges.containsKey(caseId)) {
            this.propagatedChanges.put(caseId, new CaseChangedFields(caseId, new HashMap<>(propagatedFields)));
        } else {
            this.propagatedChanges.get(caseId).mergeChanges(propagatedFields);
        }
    }

    public void mergePropagated(Map<String, CaseChangedFields> propagatedChanges) {
        propagatedChanges.forEach((id, caseChangedFields) -> addPropagated(id, caseChangedFields.getChangedFields()));
    }

    public ChangedFieldContainer mergeChangedAndPropagated() {
        Map<String, ChangedField> result = new HashMap<>();
        this.propagatedChanges.forEach((id, caseChangedFields) -> {
            Map<String, ChangedField> changes = new HashMap<>();
            caseChangedFields.getChangedFields().forEach((caseId, changedField) -> {
                changedField.getChangedOn().forEach(taskPair -> {
                    if (this.getTask() != null
                            && !taskPair.getTaskId().equals(this.getTask().getStringId())
                            && changedField.getAttributes().containsKey("behavior")) {
                        Map<String, Object> newBehavior = new HashMap<>();
                        ((Map<String, Object>) changedField.getAttributes().get("behavior")).forEach((transId, behavior) -> {
                            String behaviorChangedOnTrans = transId.equals(taskPair.getTransition()) ?
                                    this.getTask().getTransitionId() : taskPair.getTransition() + "-" + transId;
                            newBehavior.put(behaviorChangedOnTrans, behavior);
                        });
                        changedField.getAttributes().put("behavior", newBehavior);
                    }
                    if (caseChangedFields.getCaseId().equals(this.getACase().getStringId())) {
                        changes.put(caseId, changedField);
                    }
                    changes.put(taskPair.getTaskId() + "-" + caseId, changedField);
                });
            });
//            todo vytiahnúť do metódy
            changes.forEach((fieldId, changedField) -> {
                if (result.containsKey(fieldId)) {
                    result.get(fieldId).merge(changedField);
                } else {
                    result.put(fieldId, changedField);
                }
            });
        });
        ChangedFieldContainer container = new ChangedFieldContainer();
        this.changedFields.forEach((fieldId, changedField) -> {
            if (result.containsKey(fieldId)) {
                result.get(fieldId).merge(changedField);
            } else {
                result.put(fieldId, changedField);
            }
        });
        result.forEach((fieldId, changedField) -> {
            container.getChangedFields().put(fieldId, changedField.getAttributes());
        });
        return container;
    }

    private Optional<ChangedField> findInPropagated(String fieldId) {
        return findInPropagated(this.getACase().getStringId(), fieldId);
    }

    private Optional<ChangedField> findInPropagated(String caseId, String fieldId) {
        if (!propagatedChanges.containsKey(caseId)) {
            return Optional.empty();
        }

        return Optional.ofNullable(propagatedChanges.get(caseId).getChangedFields().get(fieldId));
    }
}
