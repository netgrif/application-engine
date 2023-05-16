package com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Data;

import java.util.List;

@Data
public class SetDataEventOutcome extends TaskEventOutcome {

    private DataSet changedFields = new DataSet();

    public SetDataEventOutcome() {
        super();
    }

    public SetDataEventOutcome(List<EventOutcome> outcomes) {
        this();
        this.setOutcomes(outcomes);
    }

    public SetDataEventOutcome(Case aCase, Task task) {
        super(aCase, task);
    }

    public SetDataEventOutcome(Case aCase, Task task, List<EventOutcome> outcomes) {
        this(aCase, task);
        this.setOutcomes(outcomes);
    }

    public void addChangedField(String fieldId, Field<?> field) {
        if (!changedFields.getFields().containsKey(fieldId)) {
            changedFields.getFields().put(fieldId, field);
        } else {
            changedFields.getFields().get(fieldId).applyChanges(field);
        }
    }
}
