package com.netgrif.workflow.history.domain;

public interface ITaskEventLog {

    void setTaskId(String taskId);

    void setTaskTitle(String taskTitle);

    void setTransitionId(String transitionId);

    String getTaskId();

    String getTaskTitle();

    String getTransitionId();
}