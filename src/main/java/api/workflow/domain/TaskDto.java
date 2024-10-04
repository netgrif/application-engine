package api.workflow.domain;

import api.workflow.domain.triggers.TriggerDto;
import api.petrinet.domain.I18nStringDto;
import api.petrinet.domain.dataset.FieldDto;
import api.petrinet.domain.layout.TaskLayoutDto;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class TaskDto {

    private String id;

    private String processId;

    private String caseId;

    private String transitionId;

    private TaskLayoutDto layout;

    private I18nStringDto title;

    private String caseColor;

    private String caseTitle;

    private Integer priority;

    private String userId;

    private List<TriggerDto> triggers;

    private Map<String, Map<String, Boolean>> roles;

    private Map<String, Map<String, Boolean>> userRefs;

    private Map<String, Map<String, Boolean>> users;

    private List<String> viewRoles;

    private List<String> viewUserRefs;

    private List<String> viewUsers;

    private List<String> negativeViewRoles;

    private List<String> negativeViewUsers;

    private LocalDateTime startDate;

    private LocalDateTime finishDate;

    private String finishedBy;

    private String transactionId;

    private Boolean requiredFilled;

    private LinkedHashSet<String> immediateDataFields;

    private List<FieldDto> immediateData;

    private String icon;

    private String assignPolicy;

    private String dataFocusPolicy;

    private String finishPolicy;

    private Map<String, I18nStringDto> eventTitles;

    private Map<String, Boolean> assignedUserPolicy;

    private Map<String, Integer> consumedTokens;

    public TaskDto() {
    }

    public TaskDto(String id, String processId, String caseId, String transitionId, TaskLayoutDto layout, I18nStringDto title, String caseColor, String caseTitle, Integer priority, String userId, List<TriggerDto> triggers, Map<String, Map<String, Boolean>> roles, Map<String, Map<String, Boolean>> userRefs, Map<String, Map<String, Boolean>> users, List<String> viewRoles, List<String> viewUserRefs, List<String> viewUsers, List<String> negativeViewRoles, List<String> negativeViewUsers, LocalDateTime startDate, LocalDateTime finishDate, String finishedBy, String transactionId, Boolean requiredFilled, LinkedHashSet<String> immediateDataFields, List<FieldDto> immediateData, String icon, String assignPolicy, String dataFocusPolicy, String finishPolicy, Map<String, I18nStringDto> eventTitles, Map<String, Boolean> assignedUserPolicy, Map<String, Integer> consumedTokens) {
        this.id = id;
        this.processId = processId;
        this.caseId = caseId;
        this.transitionId = transitionId;
        this.layout = layout;
        this.title = title;
        this.caseColor = caseColor;
        this.caseTitle = caseTitle;
        this.priority = priority;
        this.userId = userId;
        this.triggers = triggers;
        this.roles = roles;
        this.userRefs = userRefs;
        this.users = users;
        this.viewRoles = viewRoles;
        this.viewUserRefs = viewUserRefs;
        this.viewUsers = viewUsers;
        this.negativeViewRoles = negativeViewRoles;
        this.negativeViewUsers = negativeViewUsers;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.finishedBy = finishedBy;
        this.transactionId = transactionId;
        this.requiredFilled = requiredFilled;
        this.immediateDataFields = immediateDataFields;
        this.immediateData = immediateData;
        this.icon = icon;
        this.assignPolicy = assignPolicy;
        this.dataFocusPolicy = dataFocusPolicy;
        this.finishPolicy = finishPolicy;
        this.eventTitles = eventTitles;
        this.assignedUserPolicy = assignedUserPolicy;
        this.consumedTokens = consumedTokens;
    }

    public String getStringId() {
        return id;
    }

    public void setStringId(String id) {
        this.id = id;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getTransitionId() {
        return transitionId;
    }

    public void setTransitionId(String transitionId) {
        this.transitionId = transitionId;
    }

    public TaskLayoutDto getLayout() {
        return layout;
    }

    public void setLayout(TaskLayoutDto layout) {
        this.layout = layout;
    }

    public I18nStringDto getTitle() {
        return title;
    }

    public void setTitle(I18nStringDto title) {
        this.title = title;
    }

    public String getCaseColor() {
        return caseColor;
    }

    public void setCaseColor(String caseColor) {
        this.caseColor = caseColor;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

    public void setCaseTitle(String caseTitle) {
        this.caseTitle = caseTitle;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<TriggerDto> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<TriggerDto> triggers) {
        this.triggers = triggers;
    }

    public Map<String, Map<String, Boolean>> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, Map<String, Boolean>> roles) {
        this.roles = roles;
    }

    public Map<String, Map<String, Boolean>> getUserRefs() {
        return userRefs;
    }

    public void setUserRefs(Map<String, Map<String, Boolean>> userRefs) {
        this.userRefs = userRefs;
    }

    public Map<String, Map<String, Boolean>> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Map<String, Boolean>> users) {
        this.users = users;
    }

    public List<String> getViewRoles() {
        return viewRoles;
    }

    public void setViewRoles(List<String> viewRoles) {
        this.viewRoles = viewRoles;
    }

    public List<String> getViewUserRefs() {
        return viewUserRefs;
    }

    public void setViewUserRefs(List<String> viewUserRefs) {
        this.viewUserRefs = viewUserRefs;
    }

    public List<String> getViewUsers() {
        return viewUsers;
    }

    public void setViewUsers(List<String> viewUsers) {
        this.viewUsers = viewUsers;
    }

    public List<String> getNegativeViewRoles() {
        return negativeViewRoles;
    }

    public void setNegativeViewRoles(List<String> negativeViewRoles) {
        this.negativeViewRoles = negativeViewRoles;
    }

    public List<String> getNegativeViewUsers() {
        return negativeViewUsers;
    }

    public void setNegativeViewUsers(List<String> negativeViewUsers) {
        this.negativeViewUsers = negativeViewUsers;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(LocalDateTime finishDate) {
        this.finishDate = finishDate;
    }

    public String getFinishedBy() {
        return finishedBy;
    }

    public void setFinishedBy(String finishedBy) {
        this.finishedBy = finishedBy;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Boolean getRequiredFilled() {
        return requiredFilled;
    }

    public void setRequiredFilled(Boolean requiredFilled) {
        this.requiredFilled = requiredFilled;
    }

    public LinkedHashSet<String> getImmediateDataFields() {
        return immediateDataFields;
    }

    public void setImmediateDataFields(LinkedHashSet<String> immediateDataFields) {
        this.immediateDataFields = immediateDataFields;
    }

    public List<FieldDto> getImmediateData() {
        return immediateData;
    }

    public void setImmediateData(List<FieldDto> immediateData) {
        this.immediateData = immediateData;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAssignPolicy() {
        return assignPolicy;
    }

    public void setAssignPolicy(String assignPolicy) {
        this.assignPolicy = assignPolicy;
    }

    public String getDataFocusPolicy() {
        return dataFocusPolicy;
    }

    public void setDataFocusPolicy(String dataFocusPolicy) {
        this.dataFocusPolicy = dataFocusPolicy;
    }

    public String getFinishPolicy() {
        return finishPolicy;
    }

    public void setFinishPolicy(String finishPolicy) {
        this.finishPolicy = finishPolicy;
    }

    public Map<String, I18nStringDto> getEventTitles() {
        return eventTitles;
    }

    public void setEventTitles(Map<String, I18nStringDto> eventTitles) {
        this.eventTitles = eventTitles;
    }

    public Map<String, Boolean> getAssignedUserPolicy() {
        return assignedUserPolicy;
    }

    public void setAssignedUserPolicy(Map<String, Boolean> assignedUserPolicy) {
        this.assignedUserPolicy = assignedUserPolicy;
    }

    public Map<String, Integer> getConsumedTokens() {
        return consumedTokens;
    }

    public void setConsumedTokens(Map<String, Integer> consumedTokens) {
        this.consumedTokens = consumedTokens;
    }
}
