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

    Action(String action, String trigger){
        this.definition = action
        this.trigger = ActionTigger.fromString(trigger)
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
