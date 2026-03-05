package com.netgrif.application.engine.objects.event.events.petrinet;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import lombok.Getter;

public class ProcessDeployEvent extends ProcessEvent {

    @Getter
    protected final ImportPetriNetEventOutcome eventOutcome;

    public ProcessDeployEvent(ImportPetriNetEventOutcome eventOutcome, EventPhase eventPhase, AbstractUser user) {
        super(eventOutcome, eventPhase, user, getWorkspaceIdFromResource(eventOutcome.getNet()));
        this.eventOutcome = eventOutcome;
    }

    @Override
    public String getMessage() {
        return "ProcessDeployEvent: PetriNet [%s] imported"
                .formatted(eventOutcome.getNet() == null ? MISSING_IDENTIFIER : eventOutcome.getNet().getIdentifier());
    }
}
