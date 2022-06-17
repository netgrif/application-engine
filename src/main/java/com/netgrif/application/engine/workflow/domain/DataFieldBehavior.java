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
}
