package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Data;

@Data
public class FinishTaskEventOutcome extends TaskEventOutcome{

    public FinishTaskEventOutcome() {
    }

    public FinishTaskEventOutcome(Task task, String caseId) {
        super(task, caseId);
    }
}
