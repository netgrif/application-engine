package com.netgrif.application.engine.workflow.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskPair implements Serializable {
    private static final long serialVersionUID = -3865322078419904394L;

    private ObjectId taskId;
    private String transitionId;
    private State state;
    private String userId;

    public TaskPair(Task task) {
        this.taskId = task.getId();
        this.transitionId = task.getTransitionId();
        this.state = task.getState();
        // TODO: release/8.0.0
        this.userId = task.getAssigneeId();
    }

    public String getTaskStringId() {
        return taskId.toString();
    }
}
