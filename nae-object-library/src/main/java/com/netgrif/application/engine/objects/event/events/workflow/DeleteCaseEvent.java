package com.netgrif.application.engine.objects.event.events.workflow;

import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;

public class DeleteCaseEvent extends CaseEvent {

    public DeleteCaseEvent(DeleteCaseEventOutcome caseEventOutcome, EventPhase eventPhase) {
        super(caseEventOutcome, eventPhase, getWorkspaceIdFromResource(caseEventOutcome.getCase()));
    }

    @Override
    public String getMessage() {
        return "DeleteCaseEvent: Case [%s] deleted"
                .formatted(caseEventOutcome.getCase() == null ? MISSING_IDENTIFIER : caseEventOutcome.getCase().getStringId());
    }
}
