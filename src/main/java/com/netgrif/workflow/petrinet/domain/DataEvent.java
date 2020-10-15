package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DataEvent {

    private String id;

    private DataEventType type;

    private Map<String, List<Action>> actions;

    public DataEvent(){

    }

    public DataEvent(String id, String type) {
        this.id = id;
        this.type = DataEventType.valueOf(type.toUpperCase());
    }
}
