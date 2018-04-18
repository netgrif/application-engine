package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import org.bson.types.ObjectId


class Action {

    private ObjectId id = new ObjectId()

    private String definition

    private ActionTrigger trigger

    static Action buildAction(String action, String trigger){
        if(action == null || action.equalsIgnoreCase("") || action.equalsIgnoreCase(" ")
                || trigger == null || trigger.equalsIgnoreCase("") || trigger.equalsIgnoreCase(" "))
            return null

        return new Action(action, trigger)
    }

    Action(String definition, String trigger){
        this.definition = definition
        this.trigger = ActionTrigger.fromString(trigger)
    }

    Action(String definition, ActionTrigger trigger) {
        this.definition = definition
        this.trigger = trigger
    }

    Action() {
    }

    ObjectId getId() {
        return id
    }

    String getDefinition() {
        return definition
    }

    Boolean isTriggeredBy(ActionTrigger trigger) {
        return this.trigger == trigger
    }

    enum ActionTrigger {
        GET,
        SET

        static ActionTrigger fromString(String val){
            return ActionTrigger.valueOf(val.toUpperCase())
        }
    }
}
