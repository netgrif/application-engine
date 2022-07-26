package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.DataEventType;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;


@Data
public class DataRef {

    private Field field;

    private FieldBehavior behavior;

    private Map<DataEventType, DataEvent> events;

    private FieldLayout layout;

    private Component component;

    public DataRef() {
        this.events = new HashMap<>();
        this.layout = new FieldLayout();
    }

    public DataRef(FieldBehavior behavior, Map<DataEventType, DataEvent> events, FieldLayout layout, Component component) {
        this();
        this.behavior = behavior;
        this.events = events;
        this.layout = layout;
        if (component != null) {
            this.component = component;
        }
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

    public boolean isImmediate() {
    }
}