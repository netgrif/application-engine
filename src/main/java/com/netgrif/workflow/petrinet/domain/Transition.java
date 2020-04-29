package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.petrinet.domain.layout.TaskLayout;
import com.netgrif.workflow.petrinet.domain.policies.AssignPolicy;
import com.netgrif.workflow.petrinet.domain.policies.DataFocusPolicy;
import com.netgrif.workflow.petrinet.domain.policies.FinishPolicy;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import com.netgrif.workflow.workflow.domain.triggers.Trigger;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;
import java.util.stream.Collectors;

@Document
public class Transition extends Node {

    @Field("dataGroups")
    @Getter @Setter
    private Map<String, DataGroup> dataGroups;

    @Field("dataSet")
    @Getter @Setter
    private LinkedHashMap<String, DataFieldLogic> dataSet;

    @Field("roles")
    @Getter @Setter
    private Map<String, Set<RolePermission>> roles;

    @Field("triggers")
    @Getter @Setter
    private List<Trigger> triggers;

    @Getter @Setter
    private TaskLayout layout;

    @Getter @Setter
    private Integer priority;

    @Getter @Setter
    private AssignPolicy assignPolicy;

    @Getter @Setter
    private String icon;

    @Getter @Setter
    private DataFocusPolicy dataFocusPolicy;

    @Getter @Setter
    private FinishPolicy finishPolicy;

    @Getter @Setter
    private Map<EventType, Event> events;

    @Getter @Setter
    private String defaultRoleId;

    public Transition() {
        super();
        dataSet = new LinkedHashMap<>();
        roles = new HashMap<>();
        triggers = new LinkedList<>();
        dataGroups = new LinkedHashMap<>();
        assignPolicy = AssignPolicy.MANUAL;
        dataFocusPolicy = DataFocusPolicy.MANUAL;
        finishPolicy = FinishPolicy.MANUAL;
        events = new HashMap<>();
    }

    public void addDataSet(String field, Set<FieldBehavior> behavior, Set<Action> actions, FieldLayout layout){
        if(dataSet.containsKey(field) && dataSet.get(field) != null){
            if(behavior != null) dataSet.get(field).getBehavior().addAll(behavior);
            if(actions != null) dataSet.get(field).getActions().addAll(actions);
            if(layout != null) dataSet.get(field).setLayout(layout);
        } else {
            dataSet.put(field,new DataFieldLogic(behavior, actions, layout));
        }
    }

    public void addActions(String field, LinkedHashSet<Action> actions){
        if(dataSet.containsKey(field)){
            dataSet.get(field).addActions(actions);
        }
    }

    public void addRole(String roleId, Set<RolePermission> permissions) {
        if (roles.containsKey(roleId) && roles.get(roleId) != null) {
            roles.get(roleId).addAll(permissions);
        } else {
            roles.put(roleId, permissions);
        }
    }

    public void addDataGroup(DataGroup dataGroup) {
        dataGroups.put(dataGroup.getStringId(), dataGroup);
    }

    public void addTrigger(Trigger trigger) {
        this.triggers.add(trigger);
    }

    public boolean isDisplayable(String fieldId){
        DataFieldLogic logic = dataSet.get(fieldId);
        return logic != null && logic.isDisplayable();
    }

    public List<String> getImmediateData(){
        return dataSet.entrySet().stream().filter(entry -> entry.getValue().getBehavior().contains(FieldBehavior.IMMEDIATE))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public List<Action> getPreFinishActions() {
        return getPreActions(EventType.FINISH);
    }

    public List<Action> getPostFinishActions() {
        return getPostActions(EventType.FINISH);
    }

    public List<Action> getPreAssignActions() {
        return getPreActions(EventType.ASSIGN);
    }

    public List<Action> getPostAssignActions() {
        return getPostActions(EventType.ASSIGN);
    }

    public List<Action> getPreCancelActions() {
        return getPreActions(EventType.CANCEL);
    }

    public List<Action> getPostCancelActions() {
        return getPostActions(EventType.CANCEL);
    }

    public List<Action> getPreDelegateActions() {
        return getPreActions(EventType.DELEGATE);
    }

    public List<Action> getPostDelegateActions() {
        return getPostActions(EventType.DELEGATE);
    }

    private List<Action> getPreActions(EventType type) {
        if (events.containsKey(type))
            return events.get(type).getPreActions();
        return new LinkedList<>();
    }

    private List<Action> getPostActions(EventType type) {
        if (events.containsKey(type))
            return events.get(type).getPostActions();
        return new LinkedList<>();
    }

    public I18nString getFinishMessage() {
        return getMessage(EventType.FINISH);
    }

    public I18nString getAssignMessage() {
        return getMessage(EventType.ASSIGN);
    }

    public I18nString getCancelMessage() {
        return getMessage(EventType.CANCEL);
    }

    public I18nString getDelegateMessage() {
        return getMessage(EventType.DELEGATE);
    }

    private I18nString getMessage(EventType type) {
        if (events.containsKey(type) )
            return events.get(type).getMessage();
        return null;
    }

    @Override
    public String toString() {
        return this.getTitle().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transition that = (Transition) o;
        return importId.equals(that.importId);
    }

    public void addEvent(Event event) {
        events.put(event.getType(), event);
    }
}