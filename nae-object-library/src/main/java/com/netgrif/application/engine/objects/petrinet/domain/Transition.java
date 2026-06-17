package com.netgrif.application.engine.objects.petrinet.domain;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.objects.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.objects.petrinet.domain.events.DataEventType;
import com.netgrif.application.engine.objects.petrinet.domain.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.petrinet.domain.layout.TaskLayout;
import com.netgrif.application.engine.objects.petrinet.domain.policies.AssignPolicy;
import com.netgrif.application.engine.objects.petrinet.domain.policies.DataFocusPolicy;
import com.netgrif.application.engine.objects.petrinet.domain.policies.FinishPolicy;
import com.netgrif.application.engine.objects.utils.CopyConstructorUtil;
import com.netgrif.application.engine.objects.workflow.domain.triggers.AutoTrigger;
import com.netgrif.application.engine.objects.workflow.domain.triggers.Trigger;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

public class Transition extends Node {

    @Getter
    @Setter
    private Map<String, DataGroup> dataGroups;

    @Getter
    @Setter
    private LinkedHashMap<String, DataFieldLogic> dataSet;

    @Getter
    @Setter
    private Map<String, Map<String, Boolean>> roles;

    @Getter
    @Setter
    private List<String> negativeViewRoles;

    @Getter
    @Setter
    private Map<String, Map<String, Boolean>> actorRefs;

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

//    @Transient
    private Boolean hasAutoTrigger;

    @Getter
    @Setter
    private Map<String, String> tags;

    public Transition() {
        super();
        dataSet = new LinkedHashMap<>();
        roles = new HashMap<>();
        actorRefs = new HashMap<>();
        triggers = new LinkedList<>();
        negativeViewRoles = new LinkedList<>();
        dataGroups = new LinkedHashMap<>();
        assignPolicy = AssignPolicy.MANUAL;
        dataFocusPolicy = DataFocusPolicy.MANUAL;
        finishPolicy = FinishPolicy.MANUAL;
        events = new HashMap<>();
        assignedUserPolicy = new HashMap<>();
        tags = new HashMap<>();
    }

    public void addDataSet(String field, Set<FieldBehavior> behavior, Map<DataEventType, DataEvent> events, FieldLayout layout, Component component) {
        if (dataSet.containsKey(field) && dataSet.get(field) != null) {
            if (behavior != null) dataSet.get(field).getBehavior().addAll(behavior);
            if (events != null) dataSet.get(field).setEvents(events);
            if (layout != null) dataSet.get(field).setLayout(layout);
            if (component != null) dataSet.get(field).setComponent(component);
        } else {
            dataSet.put(field, new DataFieldLogic(behavior, events, layout, component));
        }
    }

    public void setDataEvents(String field, Map<DataEventType, DataEvent> events) {
        if (dataSet.containsKey(field)) {
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

    public void addActorRef(String actorFieldId, Map<String, Boolean> permissions) {
        if (actorFieldId == null) {
            throw new IllegalArgumentException("actorFieldId must not be null");
        }
        Map<String, Boolean> safePermissions = (permissions == null) ? new HashMap<>() : permissions;
        if (actorRefs.containsKey(actorFieldId) && actorRefs.get(actorFieldId) != null) {
            actorRefs.get(actorFieldId).putAll(safePermissions);
        } else {
            actorRefs.put(actorFieldId, safePermissions);
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

    public boolean hasAutoTrigger() {
        if (hasAutoTrigger == null) {
            hasAutoTrigger = this.getTriggers().stream().anyMatch(trigger -> trigger instanceof AutoTrigger);
        }
        return hasAutoTrigger;
    }

    public Transition(Transition transition) {
        this.setTitle(transition.getTitle() == null ? null : transition.getTitle().clone());
        this.setPosition(transition.getPosition().getX(), transition.getPosition().getY());
        this.setImportId(transition.importId);
        this.setDataGroups(transition.dataGroups == null ? null : transition.dataGroups.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> CopyConstructorUtil.copy(y.getClass(), y), LinkedHashMap::new)));
        this.setDataSet(transition.dataSet == null ? null : transition.dataSet.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y.clone(), LinkedHashMap::new)));
        this.setRoles(transition.roles == null ? null : transition.roles.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashMap<>(e.getValue()))));
        this.setNegativeViewRoles(new ArrayList<>(transition.negativeViewRoles));
        this.setActorRefs(transition.actorRefs == null ? null : transition.actorRefs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashMap<>(e.getValue()))));
        this.setTriggers(transition.triggers == null ? null : transition.triggers.stream().map(Trigger::clone).collect(Collectors.toList()));
        this.setLayout(transition.layout == null ? null : transition.layout.clone());
        this.setPriority(transition.priority);
        this.setAssignPolicy(transition.assignPolicy);
        this.setIcon(transition.icon);
        this.setDataFocusPolicy(transition.dataFocusPolicy);
        this.setFinishPolicy(transition.finishPolicy);
        this.setEvents(transition.events == null ? null : transition.events.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
        this.setAssignedUserPolicy(new HashMap<>(transition.assignedUserPolicy));
        this.setTags(new HashMap<>(transition.tags));
        this.setDefaultRoleId(transition.defaultRoleId);
    }
}
