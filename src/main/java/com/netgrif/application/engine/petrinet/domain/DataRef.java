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

    private boolean required;

    private Map<DataEventType, DataEvent> events;

    private FieldLayout layout;

    private Component component;

    public DataRef(Field field) {
        this.field = field;
        this.behavior = FieldBehavior.defaultValue();
    }

    // TODO: NAE-1645
//    public boolean isDisplayableForCase() {
//        return behavior.contains(FieldBehavior.EDITABLE) || behavior.contains(FieldBehavior.VISIBLE) || behavior.contains(FieldBehavior.HIDDEN);
//    }

    public static List<Action> getEventAction(DataEvent event, EventPhase phase){
        if(phase == null) phase = event.getDefaultPhase();
        if(phase.equals(EventPhase.PRE)){
            return event.getPreActions();
        } else {
            return event.getPostActions();
        }
    }

    // TODO: NAE-1645
    @Deprecated
    public boolean isRequired() {
        return this.required;
    }

    // TODO: NAE-1645
    @Override
    public String toString() {
        if (behavior == null) {
            return "";
        }
        return behavior.toString();
    }

    // TODO: NAE-1645
    @Deprecated
    public boolean isForbidden() {
        return behavior == FieldBehavior.FORBIDDEN;
    }

    public boolean layoutExist() {
        return this.layout != null;
    }

    public boolean isImmediate() {
        return field.isImmediate();
    }
}