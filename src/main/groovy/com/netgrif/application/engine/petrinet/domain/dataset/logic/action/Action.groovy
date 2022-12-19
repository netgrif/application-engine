package com.netgrif.application.engine.petrinet.domain.dataset.logic.action

import com.netgrif.application.engine.petrinet.domain.events.DataEventType
import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType
import org.bson.types.ObjectId


class Action {

    private String importId;

    private ObjectId id = new ObjectId()

    private Map<String, String> fieldIds = new HashMap<>()

    private Map<String, String> transitionIds = new HashMap<>()

    private String definition

    private DataEventType trigger

    Action(Map<String, String> fieldIds, Map<String, String> transitionIds, String definition, String trigger) {
        this(fieldIds, transitionIds, definition, DataEventType.fromString(trigger))
    }

    Action(String definition, String trigger) {
        this(new HashMap<String, String>(), new HashMap<String, String>(), definition, trigger)
    }

    Action(Map<String, String> fieldIds, Map<String, String> transitionIds, String definition, DataEventType trigger) {
        this.definition = definition
        this.trigger = trigger
        this.fieldIds = fieldIds
        this.transitionIds = transitionIds
    }

    Action() {
    }

    Action(String trigger) {
        this.trigger = DataEventType.fromString(trigger)
    }

    ObjectId getId() {
        return id
    }

    String getDefinition() {
        return definition
    }

    void setDefinition(String definition) {
        this.definition = definition
    }

    Boolean isTriggeredBy(DataEventType trigger) {
        return this.trigger == trigger
    }

    DataEventType getTrigger() {
        return trigger;
    }

    void setTrigger(DataEventType trigger) {
        this.trigger = trigger;
    }

    Map<String, String> getFieldIds() {
        return fieldIds
    }

    void addFieldId(String fieldName, String fieldId) {
        this.fieldIds.put(fieldName, fieldId)
    }

    Map<String, String> getTransitionIds() {
        return transitionIds
    }

    void addTransitionId(String transitionName, String transitionId) {
        this.transitionIds.put(transitionName, transitionId)
    }

    String getImportId() {
        return importId
    }

    void setImportId(String importId) {
        this.importId = importId
    }

    void setFieldIds(Map<String, String> fieldIds) {
        this.fieldIds = fieldIds
    }

    void setTransitionIds(Map<String, String> transitionIds) {
        this.transitionIds = transitionIds
    }

    @Override
    String toString() {
        return "[$trigger] $definition"
    }

    enum ActionTrigger {
        GET,
        SET

        static ActionTrigger fromString(String val) {
            if (!val)
                return null
            return valueOf(val.toUpperCase())
        }
    }

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }

    @Override
    Action clone() {
        Action clone = new Action()
        clone.setTrigger(this.trigger)
        clone.setDefinition(this.definition)
        clone.setImportId(this.importId)
        clone.setTransitionIds(new HashMap<String, String>(this.transitionIds))
        clone.setFieldIds(new HashMap<String, String>(this.fieldIds))
        return clone
    }
}