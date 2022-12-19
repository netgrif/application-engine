package com.netgrif.application.engine.petrinet.domain.events;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
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

    @Override
    public DataEvent clone() {
        DataEvent clone = new DataEvent();
        clone.setId(this.getId());
        clone.setTitle(this.getTitle() == null ? null : this.getTitle().clone());
        clone.setMessage(this.getMessage() == null ? null : this.getMessage().clone());
        clone.setPreActions(this.getPreActions() == null ? null : getPreActions().stream().map(Action::clone).collect(Collectors.toList()));
        clone.setPostActions(this.getPostActions() == null ? null : getPostActions().stream().map(Action::clone).collect(Collectors.toList()));
        clone.setType(this.type);
        return clone;
    }
}
