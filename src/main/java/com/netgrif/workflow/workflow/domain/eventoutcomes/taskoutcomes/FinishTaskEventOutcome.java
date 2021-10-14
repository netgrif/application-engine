package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;

import java.util.List;

public class FinishTaskEventOutcome extends TaskEventOutcome{

    public FinishTaskEventOutcome() {
        super();
    }

    public FinishTaskEventOutcome(Case useCase, Task task) {
        super(useCase, task);
    }

    public FinishTaskEventOutcome(Case useCase, Task task, List<EventOutcome> outcomes) {
        this(useCase, task);
        this.setOutcomes(outcomes);
    }
}
