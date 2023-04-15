package com.netgrif.application.engine.workflow.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TaskPair {
    private ObjectId taskId;
    private String transitionId;
    private State state;
    private String userId;

    public TaskPair(Task task) {
        this.taskId = task.getId();
        this.transitionId = task.getTransitionId();
        this.state = task.getState();
        this.userId = task.getUserId();
    }

    public String getTaskStringId() {
        return taskId.toString();
    }
}
