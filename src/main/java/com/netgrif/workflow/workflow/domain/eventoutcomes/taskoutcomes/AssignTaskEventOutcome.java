package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Task;
import lombok.Data;

@Data
public class AssignTaskEventOutcome extends TaskEventOutcome{

    public AssignTaskEventOutcome() {
    }

    public AssignTaskEventOutcome(Task task  ) {
        super(task);
    }
}
