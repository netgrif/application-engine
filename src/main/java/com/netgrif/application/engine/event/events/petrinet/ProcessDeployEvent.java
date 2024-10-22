package com.netgrif.application.engine.event.events.petrinet;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;

public class ProcessDeployEvent extends ProcessEvent {

    protected final ImportPetriNetEventOutcome eventOutcome;

    public ProcessDeployEvent(ImportPetriNetEventOutcome eventOutcome, EventPhase eventPhase) {
        super(eventOutcome, eventPhase);
        this.eventOutcome = eventOutcome;
    }

    @Override
    public String getMessage() {
        return "ProcessDeployEvent: PetriNet [" + eventOutcome.getNet().getIdentifier() + "] imported";
    }
}
