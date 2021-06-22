package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Task;
import lombok.Data;

@Data
public class CancelTaskEventOutcome extends TaskEventOutcome{

    public CancelTaskEventOutcome() {
    }

    public CancelTaskEventOutcome(Task task) {
        super(task);
    }
}
