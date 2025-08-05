package com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssignTaskEventOutcome extends TaskEventOutcome {

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
