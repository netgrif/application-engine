package com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class FinishTaskEventOutcome extends TaskEventOutcome {

    /**
     * Outcome flag, which is true if the task is still executable after the finish task event
     */
    protected boolean isTaskStillExecutable;

    public FinishTaskEventOutcome() {
        super();
    }

    public FinishTaskEventOutcome(Case useCase, Task task) {
        super(useCase, task);
        this.isTaskStillExecutable = isTaskStillExecutable(useCase, task);
    }

    public FinishTaskEventOutcome(Case useCase, Task task, List<EventOutcome> outcomes) {
        this(useCase, task);
        this.setOutcomes(outcomes);
    }

    protected boolean isTaskStillExecutable(Case useCase, Task task) {
        return useCase.getTasks().stream()
                .anyMatch(taskPair -> task != null && Objects.equals(taskPair.getTask(), task.getStringId()));
    }
}
