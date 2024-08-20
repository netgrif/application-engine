package com.netgrif.application.engine.event.events.workflow;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;

public class CreateCaseEvent extends CaseEvent {

    public CreateCaseEvent(CreateCaseEventOutcome caseEventOutcome) {
        super(caseEventOutcome);
    }

    @Override
    public String getMessage() {
        return "CreateCaseEvent: Case [" + caseEventOutcome.getCase().getStringId() + "] created";
    }
}
