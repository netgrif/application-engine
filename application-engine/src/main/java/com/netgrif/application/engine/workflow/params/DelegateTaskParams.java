package com.netgrif.application.engine.workflow.params;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.TaskService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * A parameter class for the {@link TaskService#delegateTask(DelegateTaskParams)} method.
 */
@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class DelegateTaskParams {

    /// String id of the task to be delegated
    private String taskId;

    /// Task object to be delegated. The state of task must be up to date.
    private Task task;

    /// Case of the subject task. The state of useCase must be up to date.
    private Case useCase;

    /// The user, who is going to be a new assignee.
    private AbstractUser newAssignee;

    /// The user's string id, who is going to be a new assignee.
    private String newAssigneeId;

    /// The user, who is performing the delegation
    private LoggedUser delegator;

    /// The user's string id, who is performing the delegation
    private String delegatorId;

    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public static class DelegateTaskParamsBuilder {
        /// Sets the {@link #task} and {@link #taskId}
        public DelegateTaskParamsBuilder task(Task task) {
            this.task = task;
            if (task != null) {
                this.taskId = task.getStringId();
            }
            return this;
        }

        /// Sets the {@link #newAssignee} and {@link #newAssigneeId}
        public DelegateTaskParamsBuilder newAssignee(AbstractUser newAssignee) {
            this.newAssignee = newAssignee;
            if (newAssignee != null) {
                this.newAssigneeId = newAssignee.getStringId();
            }
            return this;
        }

        /// Sets the {@link #delegator} and {@link #delegatorId}
        public DelegateTaskParamsBuilder delegator(LoggedUser delegator) {
            this.delegator = delegator;
            if (delegator != null) {
                this.delegatorId = delegator.getStringId();
            }
            return this;
        }
    }
}
