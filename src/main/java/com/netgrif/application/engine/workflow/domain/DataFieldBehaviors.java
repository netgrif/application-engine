package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netgrif.application.engine.mapper.filters.DataFieldBehaviorFilter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class DataFieldBehaviors {
    /**
     * TransitionId: [FieldBehavior]
     */
    @JsonInclude(content = JsonInclude.Include.CUSTOM, contentFilter = DataFieldBehaviorFilter.class)
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

    @Override
    public DataFieldBehaviors clone() {
        DataFieldBehaviors clone = new DataFieldBehaviors();
        clone.behaviors = this.behaviors.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone()));
        return clone;
    }
}
