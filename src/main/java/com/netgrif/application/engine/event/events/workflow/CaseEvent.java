package com.netgrif.application.engine.event.events.workflow;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CaseEventOutcome;
import lombok.Getter;

@Getter
public abstract class CaseEvent extends Event {

    protected final CaseEventOutcome caseEventOutcome;

    public CaseEvent(CaseEventOutcome caseEventOutcome, EventPhase eventPhase) {
        super(caseEventOutcome, eventPhase);
        this.caseEventOutcome = caseEventOutcome;
    }

}
