package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Data;

@Data
public class DelegateTaskEventOutcome extends TaskEventOutcome{

    public DelegateTaskEventOutcome() {
        super();
    }

    public DelegateTaskEventOutcome(Case useCase, Task task) {
        super(useCase, task);
    }
}
