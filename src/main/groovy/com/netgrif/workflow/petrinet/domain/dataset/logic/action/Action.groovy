package com.netgrif.workflow.petrinet.domain.dataset.logic.action


class Action {

    private String definition
    private ActionTigger trigger

    static Action buildAction(String action, String trigger){
        if(action == null || action.equalsIgnoreCase("") || action.equalsIgnoreCase(" ")
                || trigger == null || trigger.equalsIgnoreCase("") || trigger.equalsIgnoreCase(" "))
            return null

        return new Action(action, trigger)
    }

    Action(String definition, String trigger){
        this.definition = definition
        this.trigger = ActionTigger.fromString(trigger)
    }

    Action(String definition, ActionTigger trigger) {
        this.definition = definition
        this.trigger = trigger
    }

    Action() {
    }

    String getDefinition() {
        return definition
    }

    Boolean runOn(ActionTigger trigger){
        return this.trigger == trigger
    }

    enum ActionTigger {
        GET,
        SET

        static ActionTigger fromString(String val){
            return ActionTigger.valueOf(val.toUpperCase())
        }
    }
}
