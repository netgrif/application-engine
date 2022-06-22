package com.netgrif.application.engine.workflow.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class DataFieldBehavior {
    /**
     * TransitionId: [FieldBehavior]
     */
    private Map<String, Set<FieldBehavior>> behaviors;

    public DataFieldBehavior() {
        this.behaviors = new HashMap<>();
    }

    public Set<FieldBehavior> get(String transitionId) {
        return behaviors.get(transitionId);
    }

    public Set<FieldBehavior> put(String transitionId, Set<FieldBehavior> behavior) {
        return behaviors.put(transitionId, behavior);
    }

    public boolean contains(String transitionId) {
        return behaviors.containsKey(transitionId);
    }
}
