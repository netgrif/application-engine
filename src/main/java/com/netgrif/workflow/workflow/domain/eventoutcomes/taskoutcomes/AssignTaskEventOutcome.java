package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Data;

@Data
public class AssignTaskEventOutcome extends TaskEventOutcome{

    public AssignTaskEventOutcome() {
        super();
    }

    public AssignTaskEventOutcome(Case useCase, Task task) {
        super(useCase, task);
    }
}
