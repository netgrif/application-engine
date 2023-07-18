package com.netgrif.application.engine.petrinet.domain;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.DataEventType;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;


public class DataFieldLogic {

    @Getter
    @Setter
    private Set<FieldBehavior> behavior;

    @Getter
    @Setter
    private Map<DataEventType, DataEvent> events;

    @Getter
    @Setter
    private FieldLayout layout;

    @Getter
    @Setter
    private Component component;

    public DataFieldLogic() {
        this.behavior = new HashSet<>();
        this.events = new HashMap<>();
        this.layout = new FieldLayout();
    }

    public DataFieldLogic(Set<FieldBehavior> behavior, Map<DataEventType, DataEvent> events, FieldLayout layout, Component component) {
        this();
        this.behavior.addAll(behavior);
        this.events = events;
        this.layout = layout;
        if (component != null)
            this.component = component;
    }

    public ObjectNode applyBehavior(ObjectNode jsonNode) {
        behavior.forEach(fieldBehavior -> jsonNode.put(fieldBehavior.toString(), true));
        return jsonNode;
    }

    public ObjectNode applyBehavior() {
        return applyBehavior(JsonNodeFactory.instance.objectNode());
    }

    public boolean isDisplayable() {
        return behavior.contains(FieldBehavior.EDITABLE) || behavior.contains(FieldBehavior.VISIBLE) || behavior.contains(FieldBehavior.HIDDEN);
    }

    public boolean isDisplayableForCase() {
        return behavior.contains(FieldBehavior.EDITABLE) || behavior.contains(FieldBehavior.VISIBLE) || behavior.contains(FieldBehavior.HIDDEN);
    }

    public static List<Action> getEventAction(DataEvent event, EventPhase phase){
        if(phase == null) phase = event.getDefaultPhase();
        if(phase.equals(EventPhase.PRE)){
            return event.getPreActions();
        } else {
            return event.getPostActions();
        }
    }

    public boolean isRequired() {
        return behavior.contains(FieldBehavior.REQUIRED);
    }

    @Override
    public String toString() {
        return behavior.stream().map(FieldBehavior::toString).collect(Collectors.joining(", "));
    }

    public boolean isForbidden() {
        return behavior.contains(FieldBehavior.FORBIDDEN);
    }

    public boolean layoutExist() {
        return this.layout != null;
    }

    @Override
    public DataFieldLogic clone() {
        DataFieldLogic clone = new DataFieldLogic();
        clone.setBehavior(new HashSet<>(this.behavior));
        clone.setLayout(this.layout == null ? null : this.layout.clone());
        clone.setEvents(this.events == null ? null : this.events.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
        clone.setComponent(this.component == null ? null : this.component.clone());
        return clone;
    }
}