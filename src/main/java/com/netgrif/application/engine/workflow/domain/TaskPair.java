package com.netgrif.application.engine.workflow.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class TaskPair implements Serializable {

    private static final long serialVersionUID = -3865322078419904394L;
    /**
     * ObjectId
     */
    private String task;
    /**
     * Import Id
     */
    private String transition;

    public TaskPair() {
    }

    public TaskPair(String task, String transition) {
        this.task = task;
        this.transition = transition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TaskPair taskPair = (TaskPair) o;

        return transition.equals(taskPair.transition);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + transition.hashCode();
        return result;
    }
}
