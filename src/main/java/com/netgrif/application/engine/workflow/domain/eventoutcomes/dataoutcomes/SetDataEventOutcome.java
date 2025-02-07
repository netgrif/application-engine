package com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.core.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.core.workflow.domain.Case;
import com.netgrif.core.workflow.domain.Task;
import com.netgrif.core.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.core.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SetDataEventOutcome extends TaskEventOutcome {

    private Map<String, ChangedField> changedFields = new HashMap<>();

    public SetDataEventOutcome(Case aCase, Task task) {
        super(aCase, task);
    }

    public SetDataEventOutcome(Case aCase, Task task, List<EventOutcome> outcomes) {
        this(aCase, task);
        this.setOutcomes(outcomes);
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
