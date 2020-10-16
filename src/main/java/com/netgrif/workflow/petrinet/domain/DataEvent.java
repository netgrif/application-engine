package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DataEvent {

    private String id;

    private Action.ActionTrigger trigger;

    private DataEventPhase defaultPhase;

    private Map<DataEventPhase, List<Action>> actions;

    public DataEvent(){
        this.actions = new HashMap<>();
    }

    public DataEvent(String id, String type) {
        this.id = id;
        this.trigger = Action.ActionTrigger.fromString(type);
        initActions();
    }

    public DataEvent(String type) {
        this.trigger = Action.ActionTrigger.fromString(type);
        initActions();
    }

    public void resolveDefaultPhase(){
        if (trigger.equals(Action.ActionTrigger.GET))
            this.defaultPhase = DataEventPhase.PRE;
        else if (trigger.equals(Action.ActionTrigger.SET))
            this.defaultPhase = DataEventPhase.POST;
        else
            this.defaultPhase = null;
    }

    private void initActions(){
        this.actions = new HashMap<>();
        this.actions.put(DataEventPhase.PRE, new ArrayList<>());
        this.actions.put(DataEventPhase.POST, new ArrayList<>());
    }
}
