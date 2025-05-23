package com.netgrif.application.engine.workflow.domain.params;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class TaskParams {

    private String taskId;
    private Task task;
    private Case useCase;
    private String assigneeId;
    private Boolean isTransactional;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public TaskParams(Task task) {
        this.task = task;
    }

    public TaskParams(Task task, String assigneeId) {
        this.task = task;
        this.assigneeId = assigneeId;
    }

    public TaskParams(String taskId) {
        this.taskId = taskId;
    }

    public TaskParams(String taskId, String assigneeId) {
        this.taskId = taskId;
        this.assigneeId = assigneeId;
    }
}
