package com.netgrif.application.engine.workflow.domain.outcome;

import com.netgrif.application.engine.workflow.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateTasksOutcome {
    private List<Task> tasks;
    private Task autoTriggerTask;
}
