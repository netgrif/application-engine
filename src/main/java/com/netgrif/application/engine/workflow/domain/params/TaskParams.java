package com.netgrif.application.engine.workflow.domain.params;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class TaskParams {

    private String taskId;
    private Task task;
    private Case useCase;
    private IUser user;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public TaskParams(Task task) {
        this.task = task;
    }

    public TaskParams(Task task, IUser user) {
        this.task = task;
        this.user = user;
    }

    public TaskParams(String taskId) {
        this.taskId = taskId;
    }

    public TaskParams(String taskId, IUser user) {
        this.taskId = taskId;
        this.user = user;
    }
}
