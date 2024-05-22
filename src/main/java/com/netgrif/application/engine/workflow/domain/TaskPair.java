package com.netgrif.application.engine.workflow.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
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
        this.userId = task.getUserId();
    }

    public String getTaskStringId() {
        return taskId.toString();
    }
}
