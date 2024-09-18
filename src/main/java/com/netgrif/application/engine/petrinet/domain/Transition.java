package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.importer.model.DataEventType;
import com.netgrif.application.engine.importer.model.EventType;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.Event;
import com.netgrif.application.engine.petrinet.domain.layout.LayoutContainer;
import com.netgrif.application.engine.petrinet.domain.policies.AssignPolicy;
import com.netgrif.application.engine.petrinet.domain.policies.FinishPolicy;
import com.netgrif.application.engine.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import com.netgrif.application.engine.workflow.domain.DataFieldBehavior;
import com.netgrif.application.engine.workflow.domain.triggers.AutoTrigger;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.stream.Collectors;

@Document
@Getter
@Setter
public class Transition extends Node {

    private String icon;
    private LinkedHashMap<String, DataRef> dataSet;
    private Map<String, Map<RolePermission, Boolean>> permissions;
    private List<Trigger> triggers;
    private LayoutContainer layoutContainer;
    private AssignPolicy assignPolicy;
    private FinishPolicy finishPolicy;
    private Map<EventType, Event> events;
    @Transient
    private Boolean hasAutoTrigger;

    public Transition() {
        super();
        dataSet = new LinkedHashMap<>();
        triggers = new LinkedList<>();
        assignPolicy = AssignPolicy.MANUAL;
        finishPolicy = FinishPolicy.MANUAL;
        events = new HashMap<>();
    }

    public void setDataRefBehavior(Field<?> field, DataFieldBehavior behavior) {
        // TODO: release/8.0.0
//        setDataRefAttribute(field, dataRef -> {
//            field.setBehavior(this.importId, behavior);
//            dataRef.setBehavior(behavior);
//        });
    }

    public void setDataRefComponent(Field<?> field, Component component) {
        // TODO: release/8.0.0
//        setDataRefAttribute(field, dataRef -> dataRef.setComponent(component));
    }

    public void setDataEvents(String field, Map<DataEventType, DataEvent> events) {
        if (dataSet.containsKey(field)) {
            dataSet.get(field).setEvents(events);
        }
    }

    public void addRole(String roleId, Map<RolePermission, Boolean> permissions) {
//        if (roles.containsKey(roleId) && roles.get(roleId) != null) {
//            roles.get(roleId).putAll(permissions);
//        } else {
//            roles.put(roleId, permissions);
//        }
    }

    public void addNegativeViewRole(String roleId) {
//        negativeViewRoles.add(roleId);
    }

    public void addUserRef(String userRefId, Map<RolePermission, Boolean> permissions) {
//        if (userRefs.containsKey(userRefId) && userRefs.get(userRefId) != null) {
//            userRefs.get(userRefId).putAll(permissions);
//        } else {
//            userRefs.put(userRefId, permissions);
//        }
    }

    public void addTrigger(Trigger trigger) {
        this.triggers.add(trigger);
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

    private List<Action> getPreActions(EventType type) {
        if (events.containsKey(type)) {
            return events.get(type).getPreActions();
        }
        return new LinkedList<>();
    }

    private List<Action> getPostActions(EventType type) {
        if (events.containsKey(type)) {
            return events.get(type).getPostActions();
        }
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

    private I18nString getMessage(EventType type) {
        if (events.containsKey(type)) {
            return events.get(type).getMessage();
        }
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

    public LinkedHashSet<String> getImmediateData() {
        return dataSet.entrySet().stream()
                .filter(entry -> {
                    if (entry.getValue().getBehavior() == null) {
                        return false;
                    }
                    return entry.getValue().getBehavior().isImmediate();
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Transition clone() {
        // TODO: release/8.0.0
        Transition clone = new Transition();
        clone.setTitle(this.getTitle() == null ? null : this.getTitle().clone());
        clone.setImportId(this.importId);
        clone.setDataSet(this.dataSet == null ? null : dataSet.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y.clone(), LinkedHashMap::new)));
        clone.setTriggers(this.triggers == null ? null : triggers.stream().map(Trigger::clone).collect(Collectors.toList()));
        clone.setAssignPolicy(assignPolicy);
        clone.setIcon(icon);
        clone.setFinishPolicy(finishPolicy);
        clone.setEvents(this.events == null ? null : events.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
        clone.setProperties(new UniqueKeyMap<>(this.getProperties()));
        clone.setLayoutContainer(this.layoutContainer == null ? null : this.layoutContainer.clone());
        return clone;
    }
}