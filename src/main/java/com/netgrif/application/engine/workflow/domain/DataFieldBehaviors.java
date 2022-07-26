package com.netgrif.application.engine.workflow.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class DataFieldBehaviors {
    /**
     * TransitionId: [FieldBehavior]
     */
    private Map<String, DataFieldBehavior> behaviors;

    public DataFieldBehaviors() {
        this.behaviors = new HashMap<>();
    }

    public DataFieldBehavior get(String transitionId) {
        return behaviors.get(transitionId);
    }

    public DataFieldBehavior put(String transitionId, DataFieldBehavior behavior) {
        return behaviors.put(transitionId, behavior);
    }

    public boolean contains(String transitionId) {
        return behaviors.containsKey(transitionId);
    }
}
