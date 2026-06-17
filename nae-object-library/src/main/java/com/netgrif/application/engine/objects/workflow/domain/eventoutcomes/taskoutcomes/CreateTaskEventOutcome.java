package com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;

public class CreateTaskEventOutcome extends TaskEventOutcome {

    public CreateTaskEventOutcome(Case useCase, Task task) {
        super(useCase, task);
    }

}
