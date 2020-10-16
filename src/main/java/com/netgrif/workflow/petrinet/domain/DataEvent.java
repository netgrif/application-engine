package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class DataEvent {

    private String id;

    private Action.ActionTrigger trigger;

    private Map<EventPhase, List<Action>> actions;

    public DataEvent(){
        initActions();
    }

    public DataEvent(String id) {
        this();
        this.id = id;
    }

    public DataEvent(String id, String type) {
        this(id);
        this.trigger = Action.ActionTrigger.fromString(type);
    }

    public EventPhase getDefaultPhase(){
        try {
            if (trigger.equals(Action.ActionTrigger.GET))
                return EventPhase.PRE;
            else if (trigger.equals(Action.ActionTrigger.SET))
                return EventPhase.POST;
        } catch (NullPointerException e){
            log.error("Trigger for event [" + this.id + "] is not set", e);
        }
        return null;
    }

    private void initActions(){
        this.actions = new HashMap<>();
        this.actions.put(EventPhase.PRE, new ArrayList<>());
        this.actions.put(EventPhase.POST, new ArrayList<>());
    }
}
