package com.netgrif.application.engine.petrinet.domain.dataset.logic.action

import com.netgrif.application.engine.petrinet.domain.dataset.logic.ChangedField
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FrontAction
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome

class Frontend {

    Case useCase
    Optional<Task> task
    List<EventOutcome> outcomes

    Frontend(Case useCase, Optional<Task> task, List<EventOutcome> outcomes) {
        this.useCase = useCase
        this.task = task
        this.outcomes = outcomes
    }

    def methodMissing(String name, Object args) {
        SetDataEventOutcome outcome = new SetDataEventOutcome(this.useCase, this.task.orElse(null))
        outcome.addFrontAction(new FrontAction(name, args))
        this.outcomes.add(outcome)
    }
}
