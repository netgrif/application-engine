package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Task;
import lombok.Data;

@Data
public class DelegateTaskEventOutcome extends TaskEventOutcome{

    public DelegateTaskEventOutcome() {
    }

    public DelegateTaskEventOutcome(Task task, String caseId) {
        super(task, caseId);
    }
}
