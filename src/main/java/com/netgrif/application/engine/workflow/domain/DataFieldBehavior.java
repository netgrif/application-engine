package com.netgrif.application.engine.workflow.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import lombok.Data;

import static com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior.*;
import static com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior.VISIBLE;

@Data
public class DataFieldBehavior {

    private FieldBehavior behavior;

    private boolean required;

    private boolean immediate;

    public DataFieldBehavior() {
        behavior = FieldBehavior.defaultValue();
    }

    public void setBehavior(FieldBehavior behavior) {
        if (behavior == null) {
            throw new IllegalArgumentException("Behavior can not be null");
        }
        this.behavior = behavior;
    }

    public boolean isForbidden() {
        return isBehaviorSet(FORBIDDEN);
    }

    public boolean isEditable() {
        return isBehaviorSet(EDITABLE);
    }

    public boolean isHidden() {
        return isBehaviorSet(HIDDEN);
    }

    public boolean isVisible() {
        return isBehaviorSet(VISIBLE);
    }

    private boolean isBehaviorSet(FieldBehavior behavior) {
        return behavior.equals(this.behavior);
    }
}
