package com.netgrif.application.engine.petrinet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.importer.model.DataEventType;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.DataFieldBehavior;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.util.List;
import java.util.Map;

import static com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior.*;


@Data
@NoArgsConstructor
public class DataRef {

    private String fieldId;
    @Transient
    private Field<?> field;
    @Transient
    private DataFieldBehavior behavior;
    private Map<DataEventType, DataEvent> events;
    private FieldLayout layout;
    private Component component;
    @Transient
    protected String parentTaskId;
    @Transient
    protected String parentCaseId;

    public DataRef(Field<?> field, DataFieldBehavior behavior) {
        this.field = field;
        this.setBehavior(behavior);
    }

    public DataRef(Field<?> field) {
        this(field, new DataFieldBehavior());
    }

    public void setBehavior(DataFieldBehavior behavior) {
        if (behavior == null) {
            behavior = new DataFieldBehavior();
        }
        this.behavior = behavior;
    }

    public static List<Action> getEventAction(DataEvent event, EventPhase phase) {
        if (phase == null) {
            phase = event.getDefaultPhase();
        }
        if (phase.equals(EventPhase.PRE)) {
            return event.getPreActions();
        } else {
            return event.getPostActions();
        }
    }

    @Override
    public String toString() {
        return fieldId;
    }

    @JsonIgnore
    public boolean isForbidden() {
        return isBehaviorSet(FORBIDDEN);
    }

    @JsonIgnore
    public boolean isEditable() {
        return isBehaviorSet(EDITABLE);
    }

    @JsonIgnore
    public boolean isHidden() {
        return isBehaviorSet(HIDDEN);
    }

    @JsonIgnore
    public boolean isVisible() {
        return isBehaviorSet(VISIBLE);
    }

    private boolean isBehaviorSet(FieldBehavior behavior) {
        return behavior.equals(this.behavior.getBehavior());
    }

    public DataRef clone() {
        DataRef cloned = new DataRef();
        // TODO: release/8.0.0 implement
        return cloned;
    }
}