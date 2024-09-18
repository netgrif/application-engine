package com.netgrif.application.engine.petrinet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.importer.model.DataEventType;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.DataFieldBehavior;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.util.HashMap;
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
    private Component component;
    // TODO: release/8.0.0 parentCaseId
    // TODO: release/8.0.0 uniqeue key map
    private Map<String, String> properties;

    public DataRef(Field<?> field, DataFieldBehavior behavior) {
        this.field = field;
        this.fieldId = field.getImportId();
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
        cloned.setFieldId(this.fieldId);
        cloned.setField(this.field == null ? null : this.field.clone());
        cloned.setBehavior(this.behavior == null ? null : this.behavior.clone());
        cloned.setEvents(this.events == null || this.events.isEmpty() ? new HashMap<>() : new HashMap<>(this.events));
        cloned.setComponent(this.component == null ? null : this.component.clone());
        return cloned;
    }
}