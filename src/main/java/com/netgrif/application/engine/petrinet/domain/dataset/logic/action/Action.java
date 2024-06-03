package com.netgrif.application.engine.petrinet.domain.dataset.logic.action;

import com.netgrif.application.engine.importer.model.DataEventType;
import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class Action implements Serializable {

    private static final long serialVersionUID = 3687481049847555522L;

    private String importId;
    private ObjectId id = new ObjectId();
    // TODO: release/8.0.0 remove both maps, deprecated
    private Map<String, String> fieldIds = new HashMap<>();
    private Map<String, String> transitionIds = new HashMap<>();
    private String definition;
    private DataEventType trigger;
    // TODO: release/8.0.0 replace with set action type
    private SetDataType setDataType = SetDataType.VALUE;

    public Action(Map<String, String> fieldIds, Map<String, String> transitionIds, String definition, String trigger) {
        this(fieldIds, transitionIds, definition, DataEventType.fromValue(trigger));
    }

    public Action(String definition, String trigger) {
        this(new HashMap<>(), new HashMap<>(), definition, trigger);
    }

    public Action(Map<String, String> fieldIds, Map<String, String> transitionIds, String definition, DataEventType trigger) {
        this.definition = definition;
        this.trigger = trigger;
        this.fieldIds = fieldIds;
        this.transitionIds = transitionIds;
    }

    public Action() {
    }

    public Action(String trigger) {
        this.trigger = DataEventType.fromValue(trigger);
    }

    public boolean isTriggeredBy(DataEventType trigger) {
        return this.trigger == trigger;
    }

    public void addFieldId(String fieldName, String fieldId) {
        this.fieldIds.put(fieldName, fieldId);
    }

    public void addTransitionId(String transitionName, String transitionId) {
        this.transitionIds.put(transitionName, transitionId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", trigger, definition);
    }

    @Override
    public Action clone() {
        Action clone = new Action();
        clone.setId(new ObjectId(this.getId().toString()));
        clone.setTrigger(this.trigger);
        clone.setDefinition(this.definition);
        clone.setImportId(this.importId);
        clone.setTransitionIds(new HashMap<String, String>(this.transitionIds));
        clone.setFieldIds(new HashMap<String, String>(this.fieldIds));
        return clone;
    }
}