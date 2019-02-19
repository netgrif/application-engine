package com.netgrif.workflow.history.domain;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@NoArgsConstructor
public class TaskEventLog extends EventLog implements ITaskEventLog {

    private String taskId;

    private String taskTitle;

    private String transitionId;

    private String caseId;

    private String caseTitle;

    public TaskEventLog(Task task, Case useCase) {
        this.taskId = task.getStringId();
        this.taskTitle = task.getTitle().toString();
        this.transitionId = task.getTransitionId();
        this.caseId = useCase.getStringId();
        this.caseTitle = useCase.getTitle();
    }

    @Override
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    @Override
    public void setTransitionId(String transitionId) {
        this.transitionId = transitionId;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public String getTaskTitle() {
        return taskTitle;
    }

    @Override
    public String getTransitionId() {
        return transitionId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setCaseTitle(String caseTitle) {
        this.caseTitle = caseTitle;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getCaseTitle() {
        return caseTitle;
    }
}