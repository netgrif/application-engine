package com.netgrif.application.engine.objects.event.events.workflow;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;

public class CreateCaseEvent extends CaseEvent {

    public CreateCaseEvent(CreateCaseEventOutcome caseEventOutcome, EventPhase phase, AbstractUser user) {
        super(caseEventOutcome, phase, user, getWorkspaceIdFromResource(caseEventOutcome.getCase()));
    }

    @Override
    public String getMessage() {
        return "CreateCaseEvent: Case [%s] created"
                .formatted(caseEventOutcome.getCase() == null ? MISSING_IDENTIFIER : caseEventOutcome.getCase().getStringId());
    }
}
