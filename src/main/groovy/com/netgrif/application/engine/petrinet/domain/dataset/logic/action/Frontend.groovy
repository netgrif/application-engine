package com.netgrif.application.engine.petrinet.domain.dataset.logic.action

import com.netgrif.application.engine.petrinet.domain.dataset.logic.ChangedField
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FrontAction
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome

class Frontend {

    private static final String FRONTEND_ACTIONS_KEY = "_frontend_actions"
    private static final String ACTION = "action"

    Case useCase
    Optional<Task> task
    List<EventOutcome> outcomes

    Frontend(Case useCase, Optional<Task> task, List<EventOutcome> outcomes) {
        this.useCase = useCase
        this.task = task
        this.outcomes = outcomes
    }

    def methodMissing(String name, Object args) {
        ChangedField changedField = new ChangedField(FRONTEND_ACTIONS_KEY)
        changedField.addAttribute(ACTION, new FrontAction(name, args))
        SetDataEventOutcome outcome = new SetDataEventOutcome(this.useCase, this.task.orElse(null))
        outcome.addChangedField(FRONTEND_ACTIONS_KEY, changedField)
        this.outcomes.add(outcome)
    }
}
