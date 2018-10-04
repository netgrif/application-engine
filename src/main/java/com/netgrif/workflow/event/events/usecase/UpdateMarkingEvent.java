package com.netgrif.workflow.event.events.usecase;

import com.netgrif.workflow.workflow.domain.Case;

public class UpdateMarkingEvent extends CaseEvent {

    public UpdateMarkingEvent(Case useCase) {
        super(useCase);
    }

    @Override
    public String getMessage() {
        return "Aktualizované značkovanie na prípade " + getCase().getTitle();
    }
}