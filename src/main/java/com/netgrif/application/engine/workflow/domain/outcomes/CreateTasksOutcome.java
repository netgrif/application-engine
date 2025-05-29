package com.netgrif.application.engine.workflow.domain.outcomes;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateTasksOutcome {
    private Case useCase;
    private List<Task> tasks;
    private Task autoTriggerTask;
}
