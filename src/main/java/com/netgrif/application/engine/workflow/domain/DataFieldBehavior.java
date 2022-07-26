package com.netgrif.application.engine.workflow.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import lombok.Data;

@Data
public class DataFieldBehavior {

    private FieldBehavior behavior;

    private boolean required;

    private boolean immediate;

    public DataFieldBehavior() {
        behavior = FieldBehavior.EDITABLE;
    }

    public void setBehavior(FieldBehavior behavior) {
        if (behavior == null) {
            throw new IllegalArgumentException("Behavior can not be null");
        }
        this.behavior = behavior;
    }
}
