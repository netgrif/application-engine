package com.netgrif.application.engine.event.events.workflow;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;

public class CreateCaseEvent extends CaseEvent {

    public CreateCaseEvent(CreateCaseEventOutcome caseEventOutcome, EventPhase phase) {
        super(caseEventOutcome, phase);
    }

    @Override
    public String getMessage() {
        return "CreateCaseEvent: Case [" + caseEventOutcome.getCase().getStringId() + "] created";
    }
}
