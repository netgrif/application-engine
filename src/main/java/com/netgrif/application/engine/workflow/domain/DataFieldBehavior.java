package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import lombok.Data;

import static com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior.*;
import static com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior.VISIBLE;

@Data
public class DataFieldBehavior {

    @JsonIgnore
    private FieldBehavior behavior;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean required;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
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

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isForbidden() {
        return isBehaviorSet(FORBIDDEN);
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isEditable() {
        return isBehaviorSet(EDITABLE);
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isHidden() {
        return isBehaviorSet(HIDDEN);
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isVisible() {
        return isBehaviorSet(VISIBLE);
    }

    public boolean hasNonDefaultBehaviourSet() {
        return !isEditable() || isVisible() || isHidden() || isForbidden() || isRequired() || isImmediate();
    }

    private boolean isBehaviorSet(FieldBehavior behavior) {
        return behavior.equals(this.behavior);
    }

    @Override
    public DataFieldBehavior clone() {
        DataFieldBehavior clone = new DataFieldBehavior();
        clone.behavior = this.behavior;
        clone.required = this.required;
        clone.immediate = this.immediate;
        return clone;
    }
}
