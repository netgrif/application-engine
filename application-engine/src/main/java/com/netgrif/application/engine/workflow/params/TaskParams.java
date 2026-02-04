package com.netgrif.application.engine.workflow.params;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.TaskService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * A parameter class for the {@link TaskService} interface's methods
 */
@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class TaskParams {

    /// String id of the subject task
    private String taskId;

    /// Subject task object. The state of task must be up to date.
    private Task task;

    /// Case of the subject task. The state of useCase must be up to date.
    private Case useCase;

    /// Subject user for the specific task event (new assignee or current assignee)
    private AbstractUser user;

    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public TaskParams(Task task) {
        this(task, null);
    }

    public TaskParams(Task task, AbstractUser user) {
        this.task = task;
        if (task != null) {
            this.taskId = task.getStringId();
        }
        this.user = user;
        this.params = new HashMap<>();
    }

    public TaskParams(String taskId) {
        this(taskId, null);
    }

    public TaskParams(String taskId, AbstractUser user) {
        this.taskId = taskId;
        this.user = user;
        this.params = new HashMap<>();
    }

    public static class TaskParamsBuilder {
        /// Sets the {@link #task} and {@link #taskId}
        public TaskParams.TaskParamsBuilder task(Task task) {
            this.task = task;
            if (task != null) {
                this.taskId = task.getStringId();
            }
            return this;
        }
    }
}
