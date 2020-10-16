package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DataEvent {

    private String id;

    private Action.ActionTrigger trigger;

    private Map<String, List<Action>> actions;

    public DataEvent(){

    }

    public DataEvent(String id, String type) {
        this.id = id;
        this.trigger = Action.ActionTrigger.fromString(type);
    }
}
