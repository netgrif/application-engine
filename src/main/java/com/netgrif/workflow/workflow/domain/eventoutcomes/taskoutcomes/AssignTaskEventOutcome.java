package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;

@Data
public class AssignTaskEventOutcome extends TaskEventOutcome{

    public AssignTaskEventOutcome() {
        super();
    }

    public AssignTaskEventOutcome(Case useCase, Task task) {
        super(useCase, task);
    }

    public AssignTaskEventOutcome(Case useCase, Task task, List<EventOutcome> outcomes) {
        this(useCase, task);
        this.setOutcomes(outcomes);
    }
}
