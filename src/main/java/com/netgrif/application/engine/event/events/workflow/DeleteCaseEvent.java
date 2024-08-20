package com.netgrif.application.engine.event.events.workflow;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;

public class DeleteCaseEvent extends CaseEvent {

    public DeleteCaseEvent(DeleteCaseEventOutcome caseEventOutcome) {
        super(caseEventOutcome);
    }

    @Override
    public String getMessage() {
        return "DeleteCaseEvent: Case [" + caseEventOutcome.getCase().getStringId() + "] deleted";
    }
}
