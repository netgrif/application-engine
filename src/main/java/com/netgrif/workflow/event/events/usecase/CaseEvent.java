package com.netgrif.workflow.event.events.usecase;

import com.netgrif.workflow.event.events.Event;
import com.netgrif.workflow.workflow.domain.Case;

public abstract class CaseEvent extends Event {

    public CaseEvent(Case useCase) {
        super(useCase);
    }

    public Case getCase() {
        return (Case) source;
    }
}