package api.petrinet.domain;

import api.workflow.domain.triggers.TriggerDto;
import api.petrinet.domain.events.EventDto;
import api.petrinet.domain.layout.TaskLayoutDto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TransitionDto extends NodeDto {

    private Map<String, DataGroupDto> dataGroups;

    private LinkedHashMap<String, DataFieldLogicDto> dataSet;

    private Map<String, Map<String, Boolean>> roles;

    private List<String> negativeViewRoles;

    private Map<String, Map<String, Boolean>> userRefs;

    private List<TriggerDto> triggers;

    private TaskLayoutDto layout;

    private Integer priority;

    private String assignPolicy;

    private String icon;

    private String dataFocusPolicy;

    private String finishPolicy;

    private Map<String, EventDto> events;

    private Map<String, Boolean> assignedUserPolicy;

    private String defaultRoleId;

    public TransitionDto() {
    }

    public TransitionDto(String id) {
        super(id);
    }

    public TransitionDto(PositionDto position, I18nStringDto title) {
        super(position, title);
    }

    public TransitionDto(String id, PositionDto position, I18nStringDto title) {
        super(id, position, title);
    }

    public TransitionDto(Map<String, DataGroupDto> dataGroups, LinkedHashMap<String, DataFieldLogicDto> dataSet, Map<String, Map<String, Boolean>> roles, List<String> negativeViewRoles, Map<String, Map<String, Boolean>> userRefs, List<TriggerDto> triggers, TaskLayoutDto layout, Integer priority, String assignPolicy, String icon, String dataFocusPolicy, String finishPolicy, Map<String, EventDto> events, Map<String, Boolean> assignedUserPolicy, String defaultRoleId) {
        this.dataGroups = dataGroups;
        this.dataSet = dataSet;
        this.roles = roles;
        this.negativeViewRoles = negativeViewRoles;
        this.userRefs = userRefs;
        this.triggers = triggers;
        this.layout = layout;
        this.priority = priority;
        this.assignPolicy = assignPolicy;
        this.icon = icon;
        this.dataFocusPolicy = dataFocusPolicy;
        this.finishPolicy = finishPolicy;
        this.events = events;
        this.assignedUserPolicy = assignedUserPolicy;
        this.defaultRoleId = defaultRoleId;
    }

    public TransitionDto(String id, Map<String, DataGroupDto> dataGroups, LinkedHashMap<String, DataFieldLogicDto> dataSet, Map<String, Map<String, Boolean>> roles, List<String> negativeViewRoles, Map<String, Map<String, Boolean>> userRefs, List<TriggerDto> triggers, TaskLayoutDto layout, Integer priority, String assignPolicy, String icon, String dataFocusPolicy, String finishPolicy, Map<String, EventDto> events, Map<String, Boolean> assignedUserPolicy, String defaultRoleId) {
        super(id);
        this.dataGroups = dataGroups;
        this.dataSet = dataSet;
        this.roles = roles;
        this.negativeViewRoles = negativeViewRoles;
        this.userRefs = userRefs;
        this.triggers = triggers;
        this.layout = layout;
        this.priority = priority;
        this.assignPolicy = assignPolicy;
        this.icon = icon;
        this.dataFocusPolicy = dataFocusPolicy;
        this.finishPolicy = finishPolicy;
        this.events = events;
        this.assignedUserPolicy = assignedUserPolicy;
        this.defaultRoleId = defaultRoleId;
    }

    public TransitionDto(PositionDto position, I18nStringDto title, Map<String, DataGroupDto> dataGroups, LinkedHashMap<String, DataFieldLogicDto> dataSet, Map<String, Map<String, Boolean>> roles, List<String> negativeViewRoles, Map<String, Map<String, Boolean>> userRefs, List<TriggerDto> triggers, TaskLayoutDto layout, Integer priority, String assignPolicy, String icon, String dataFocusPolicy, String finishPolicy, Map<String, EventDto> events, Map<String, Boolean> assignedUserPolicy, String defaultRoleId) {
        super(position, title);
        this.dataGroups = dataGroups;
        this.dataSet = dataSet;
        this.roles = roles;
        this.negativeViewRoles = negativeViewRoles;
        this.userRefs = userRefs;
        this.triggers = triggers;
        this.layout = layout;
        this.priority = priority;
        this.assignPolicy = assignPolicy;
        this.icon = icon;
        this.dataFocusPolicy = dataFocusPolicy;
        this.finishPolicy = finishPolicy;
        this.events = events;
        this.assignedUserPolicy = assignedUserPolicy;
        this.defaultRoleId = defaultRoleId;
    }

    public TransitionDto(String id, PositionDto position, I18nStringDto title, Map<String, DataGroupDto> dataGroups, LinkedHashMap<String, DataFieldLogicDto> dataSet, Map<String, Map<String, Boolean>> roles, List<String> negativeViewRoles, Map<String, Map<String, Boolean>> userRefs, List<TriggerDto> triggers, TaskLayoutDto layout, Integer priority, String assignPolicy, String icon, String dataFocusPolicy, String finishPolicy, Map<String, EventDto> events, Map<String, Boolean> assignedUserPolicy, String defaultRoleId) {
        super(id, position, title);
        this.dataGroups = dataGroups;
        this.dataSet = dataSet;
        this.roles = roles;
        this.negativeViewRoles = negativeViewRoles;
        this.userRefs = userRefs;
        this.triggers = triggers;
        this.layout = layout;
        this.priority = priority;
        this.assignPolicy = assignPolicy;
        this.icon = icon;
        this.dataFocusPolicy = dataFocusPolicy;
        this.finishPolicy = finishPolicy;
        this.events = events;
        this.assignedUserPolicy = assignedUserPolicy;
        this.defaultRoleId = defaultRoleId;
    }

    public Map<String, DataGroupDto> getDataGroups() {
        return dataGroups;
    }

    public void setDataGroups(Map<String, DataGroupDto> dataGroups) {
        this.dataGroups = dataGroups;
    }

    public LinkedHashMap<String, DataFieldLogicDto> getDataSet() {
        return dataSet;
    }

    public void setDataSet(LinkedHashMap<String, DataFieldLogicDto> dataSet) {
        this.dataSet = dataSet;
    }

    public Map<String, Map<String, Boolean>> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, Map<String, Boolean>> roles) {
        this.roles = roles;
    }

    public List<String> getNegativeViewRoles() {
        return negativeViewRoles;
    }

    public void setNegativeViewRoles(List<String> negativeViewRoles) {
        this.negativeViewRoles = negativeViewRoles;
    }

    public Map<String, Map<String, Boolean>> getUserRefs() {
        return userRefs;
    }

    public void setUserRefs(Map<String, Map<String, Boolean>> userRefs) {
        this.userRefs = userRefs;
    }

    public List<TriggerDto> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<TriggerDto> triggers) {
        this.triggers = triggers;
    }

    public TaskLayoutDto getLayout() {
        return layout;
    }

    public void setLayout(TaskLayoutDto layout) {
        this.layout = layout;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getAssignPolicy() {
        return assignPolicy;
    }

    public void setAssignPolicy(String assignPolicy) {
        this.assignPolicy = assignPolicy;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public Map<String, EventDto> getEvents() {
        return events;
    }

    public void setEvents(Map<String, EventDto> events) {
        this.events = events;
    }

    public Map<String, Boolean> getAssignedUserPolicy() {
        return assignedUserPolicy;
    }

    public void setAssignedUserPolicy(Map<String, Boolean> assignedUserPolicy) {
        this.assignedUserPolicy = assignedUserPolicy;
    }

    public String getDefaultRoleId() {
        return defaultRoleId;
    }

    public void setDefaultRoleId(String defaultRoleId) {
        this.defaultRoleId = defaultRoleId;
    }
}
