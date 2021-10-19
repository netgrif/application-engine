package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Data;

@Data
public class FinishTaskEventOutcome extends TaskEventOutcome{

    public FinishTaskEventOutcome() {
        super();
    }

    public FinishTaskEventOutcome(Case useCase, Task task) {
        super(useCase, task);
    }
}
