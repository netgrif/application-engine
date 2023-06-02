package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.DataEventType;
import com.netgrif.application.engine.petrinet.domain.events.Event;
import com.netgrif.application.engine.petrinet.domain.events.EventType;
import com.netgrif.application.engine.petrinet.domain.layout.TaskLayout;
import com.netgrif.application.engine.petrinet.domain.policies.AssignPolicy;
import com.netgrif.application.engine.petrinet.domain.policies.DataFocusPolicy;
import com.netgrif.application.engine.petrinet.domain.policies.FinishPolicy;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.analysis.CharArrayMap;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;
import java.util.stream.Collectors;

@Document
public class Transition extends Node {

    @Field("dataGroups")
    @Getter
    @Setter
    private Map<String, DataGroup> dataGroups;

    @Field("dataSet")
    @Getter
    @Setter
    private LinkedHashMap<String, DataFieldLogic> dataSet;

    @Field("roles")
    @Getter
    @Setter
    private Map<String, Map<String, Boolean>> roles;

    @Getter
    @Setter
    private List<String> negativeViewRoles;

    @Field("users")
    @Getter
    @Setter
    private Map<String, Map<String, Boolean>> userRefs;

    @Field("triggers")
    @Getter
    @Setter
    private List<Trigger> triggers;

    @Getter
    @Setter
    private TaskLayout layout;

    @Getter
    @Setter
    private Integer priority;

    @Getter
    @Setter
    private AssignPolicy assignPolicy;

    @Getter
    @Setter
    private String icon;

    @Getter
    @Setter
    private DataFocusPolicy dataFocusPolicy;

    @Getter
    @Setter
    private FinishPolicy finishPolicy;

    @Getter
    @Setter
    private Map<EventType, Event> events;

    @Getter
    @Setter
    private Map<String, Boolean> assignedUserPolicy;

    @Getter
    @Setter
    private String defaultRoleId;

    public Transition() {
        super();
        dataSet = new LinkedHashMap<>();
        roles = new HashMap<>();
        userRefs = new HashMap<>();
        triggers = new LinkedList<>();
        negativeViewRoles = new LinkedList<>();
        dataGroups = new LinkedHashMap<>();
        assignPolicy = AssignPolicy.MANUAL;
        dataFocusPolicy = DataFocusPolicy.MANUAL;
        finishPolicy = FinishPolicy.MANUAL;
        events = new HashMap<>();
        assignedUserPolicy = new HashMap<>();
    }

    public void addDataSet(String field, Set<FieldBehavior> behavior, Map<DataEventType, DataEvent> events, FieldLayout layout, Component component){
        if(dataSet.containsKey(field) && dataSet.get(field) != null){
            if(behavior != null) dataSet.get(field).getBehavior().addAll(behavior);
            if(events != null) dataSet.get(field).setEvents(events);
            if(layout != null) dataSet.get(field).setLayout(layout);
            if(component != null) dataSet.get(field).setComponent(component);
        } else {
            dataSet.put(field, new DataFieldLogic(behavior, events, layout, component));
        }
    }

    public void setDataEvents(String field, Map<DataEventType, DataEvent> events){
        if(dataSet.containsKey(field)){
            dataSet.get(field).setEvents(events);
        }
    }

    public void addRole(String roleId, Map<String, Boolean> permissions) {
        if (roles.containsKey(roleId) && roles.get(roleId) != null) {
            roles.get(roleId).putAll(permissions);
        } else {
            roles.put(roleId, permissions);
        }
    }

    public void addNegativeViewRole(String roleId) {
        negativeViewRoles.add(roleId);
    }

    public void addUserRef(String userRefId, Map<String, Boolean> permissions) {
        if (userRefs.containsKey(userRefId) && userRefs.get(userRefId) != null) {
            userRefs.get(userRefId).putAll(permissions);
        } else {
            userRefs.put(userRefId, permissions);
        }
    }

    public void addDataGroup(DataGroup dataGroup) {
        dataGroups.put(dataGroup.getStringId(), dataGroup);
    }

    public void addTrigger(Trigger trigger) {
        this.triggers.add(trigger);
    }

    public boolean isDisplayable(String fieldId) {
        DataFieldLogic logic = dataSet.get(fieldId);
        return logic != null && logic.isDisplayable();
    }

    public List<String> getImmediateData() {
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
        if (events.containsKey(type))
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

    @Override
    public Transition clone() {
        Transition clone = new Transition();
        clone.setTitle(this.getTitle() == null ? null : this.getTitle().clone());
        clone.setPosition(this.getPosition().getX(), this.getPosition().getY());
        clone.setImportId(this.importId);
        clone.setDataGroups(this.dataGroups == null ? null : dataGroups.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y.clone(), LinkedHashMap::new)));
        clone.setDataSet(this.dataSet == null ? null : dataSet.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y.clone(), LinkedHashMap::new)));
        clone.setRoles(this.roles == null ? null : roles.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashMap<>(e.getValue()))));
        clone.setNegativeViewRoles(new ArrayList<>(negativeViewRoles));
        clone.setUserRefs(this.userRefs == null ? null : userRefs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashMap<>(e.getValue()))));
        clone.setTriggers(this.triggers == null ? null : triggers.stream().map(Trigger::clone).collect(Collectors.toList()));
        clone.setLayout(this.layout == null ? null : layout.clone());
        clone.setPriority(priority);
        clone.setAssignPolicy(assignPolicy);
        clone.setAssignedUserPolicy(assignedUserPolicy);
        clone.setIcon(icon);
        clone.setDataFocusPolicy(dataFocusPolicy);
        clone.setFinishPolicy(finishPolicy);
        clone.setEvents(this.events == null ? null : events.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
        clone.setAssignedUserPolicy(new HashMap<>(assignedUserPolicy));
        clone.setDefaultRoleId(defaultRoleId);
        return clone;
    }
}