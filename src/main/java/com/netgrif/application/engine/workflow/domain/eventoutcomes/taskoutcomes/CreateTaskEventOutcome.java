package com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.adapter.workflow.domain.Case;
import com.netgrif.adapter.workflow.domain.Task;

public class CreateTaskEventOutcome extends TaskEventOutcome {

    public CreateTaskEventOutcome(Case useCase, Task task) {
        super(useCase, task);
    }

}
