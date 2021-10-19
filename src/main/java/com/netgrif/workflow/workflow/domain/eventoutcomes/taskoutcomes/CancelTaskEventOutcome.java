package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Data;

@Data
public class CancelTaskEventOutcome extends TaskEventOutcome{

    public CancelTaskEventOutcome() {
        super();
    }

    public CancelTaskEventOutcome(Case useCase, Task task) {
        super(useCase, task);
    }
}
