package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import org.bson.types.ObjectId


class Action {

    private ObjectId id = new ObjectId()

    private Map<String, String> fieldIds = new HashMap<>()

    private Map<String, String> transitionIds = new HashMap<>()

    private String definition

    private ActionTrigger trigger

    Action(Map<String, String> fieldIds, Map<String, String> transitionIds, String definition, String trigger) {
        this(fieldIds, transitionIds, definition, ActionTrigger.fromString(trigger))
    }

    Action(String definition, String trigger) {
        this(new HashMap<>(), new HashMap<>(), definition, trigger)
    }

    Action(Map<String, String> fieldIds, Map<String, String> transitionIds, String definition, ActionTrigger trigger) {
        this.definition = definition
        this.trigger = trigger
        this.fieldIds = fieldIds
        this.transitionIds = transitionIds
    }

    Action() {
    }

    Action(String trigger) {
        this.trigger = ActionTrigger.fromString(trigger)
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

    Boolean isTriggeredBy(ActionTrigger trigger) {
        return this.trigger == trigger
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

    @Override
    String toString() {
        return "[$trigger] $definition"
    }

    enum ActionTrigger {
        GET,
        SET

        static ActionTrigger fromString(String val) {
            return valueOf(val.toUpperCase())
        }
    }


}
