package com.netgrif.workflow.history.domain;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Document
public class UserTaskEventLog extends EventLog implements IUserEventLog, ITaskEventLog, ICaseEventLog {

    private String email;

    private String taskId;

    private String taskTitle;

    private String caseId;

    private String caseTitle;

    private String transitionId;

    @Field("activePlaces")
    private Map<String, Integer> activePlaces;

    @Field("dataSetValues")
    private Map<String, DataField> dataSetValues;

    public UserTaskEventLog(Task task, Case useCase) {
        this.taskId = task.getStringId();
        this.taskTitle = task.getTitle().toString();
        this.transitionId = task.getTransitionId();

        this.caseId = useCase.getStringId();
        this.caseTitle = useCase.getTitle();
        this.activePlaces = useCase.getActivePlaces();
    }

    public UserTaskEventLog() {
    }

    @Override
    public void setTaskId(String taskId) {

    }

    @Override
    public void setTaskTitle(String taskTitle) {

    }

    @Override
    public void setTransitionId(String transitionId) {

    }

    @Override
    public void setCaseId(String caseId) {

    }

    @Override
    public void setCaseTitle(String caseTitle) {

    }

    @Override
    public void setActivePlaces(Map<String, Integer> places) {

    }

    @Override
    public void setDataSetValues(Map<String, DataField> values) {

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

    @Override
    public String getCaseId() {
        return caseId;
    }

    @Override
    public String getCaseTitle() {
        return caseTitle;
    }

    @Override
    public Map<String, Integer> getActivePlaces() {
        return activePlaces;
    }

    @Override
    public Map<String, DataField> getDataSetValues() {
        return dataSetValues;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getEmail() {
        return email;
    }
}