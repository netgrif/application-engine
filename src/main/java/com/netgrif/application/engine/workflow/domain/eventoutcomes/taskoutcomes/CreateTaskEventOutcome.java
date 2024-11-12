package com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;

public class CreateTaskEventOutcome extends TaskEventOutcome {

    public CreateTaskEventOutcome(Case useCase, Task task) {
        super(useCase, task);
    }

}
