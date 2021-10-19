package com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class SetDataEventOutcome extends TaskEventOutcome {

    @Getter
    @Setter
    private Map<String, ChangedField> changedFields = new HashMap<>();

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
    }
}
