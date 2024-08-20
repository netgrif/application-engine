package com.netgrif.application.engine.event.events.petrinet;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;

public class ProcessDeployEvent extends Event {

    protected final ImportPetriNetEventOutcome eventOutcome;

    public ProcessDeployEvent(ImportPetriNetEventOutcome eventOutcome) {
        super(eventOutcome);
        this.eventOutcome = eventOutcome;
    }

    @Override
    public String getMessage() {
        return "ProcessDeployEvent: PetriNet [" + eventOutcome.getNet().getIdentifier() + "] imported";
    }
}
