package com.netgrif.application.engine.petrinet.domain.events;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
public class DataEvent extends BaseEvent {

    private DataEventType type;

    public DataEvent() {
        initActions();
    }

    public DataEvent(String id) {
        this();
        this.setId(id);
    }

    public DataEvent(String id, String type) {
        this(id);
        this.type = DataEventType.fromString(type);
    }

    public EventPhase getDefaultPhase() {
        try {
            if (type.equals(DataEventType.GET))
                return EventPhase.PRE;
            else if (type.equals(DataEventType.SET))
                return EventPhase.POST;
        } catch (NullPointerException e){
            log.error("Trigger for event [" + this.getId() + "] is not set", e);
        }
        return null;
    }

    private void initActions(){
        this.setPreActions(new ArrayList<>());
        this.setPostActions(new ArrayList<>());
    }

    public void addToActionsByDefaultPhase(List<Action> actionList){
        actionList.forEach(this::addToActionsByDefaultPhase);
    }

    public void addToActionsByDefaultPhase(Action action){
        if (getDefaultPhase() == EventPhase.PRE) {
            this.getPreActions().add(action);
        } else {
            this.getPostActions().add(action);
        }
    }

    public DataEvent clone() {
        DataEvent cloned = new DataEvent(this.getId());
        cloned.type = this.type;
        cloned.setTitle(this.getTitle());
        cloned.setMessage(this.getMessage());
        // TODO: release/7.0.0
        cloned.setPreActions(this.getPreActions());
        cloned.setPostActions(this.getPostActions());
        return cloned;
    }
}
