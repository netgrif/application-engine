package com.netgrif.application.engine.objects.event.events.workflow;

import com.netgrif.application.engine.objects.event.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CaseEventOutcome;
import lombok.Getter;

@Getter
public abstract class CaseEvent extends Event {

    protected final CaseEventOutcome caseEventOutcome;

    protected CaseEvent(CaseEventOutcome caseEventOutcome, EventPhase eventPhase, String workspaceId) {
        super(caseEventOutcome, eventPhase, workspaceId);
        this.caseEventOutcome = caseEventOutcome;
    }

}
