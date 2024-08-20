package com.netgrif.application.engine.event.events.workflow;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CaseEventOutcome;

public abstract class CaseEvent extends Event {

    protected final CaseEventOutcome caseEventOutcome;

    public CaseEvent(CaseEventOutcome caseEventOutcome) {
        super(caseEventOutcome);
        this.caseEventOutcome = caseEventOutcome;
    }

}
