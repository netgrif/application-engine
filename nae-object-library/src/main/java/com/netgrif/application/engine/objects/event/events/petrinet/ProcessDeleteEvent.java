package com.netgrif.application.engine.objects.event.events.petrinet;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import lombok.Getter;

@Getter
public class ProcessDeleteEvent extends ProcessEvent {

    protected PetriNet petriNet;

    public ProcessDeleteEvent(PetriNet petriNet, EventPhase phase, AbstractUser user) {
        super(petriNet, phase, user, getWorkspaceIdFromResource(petriNet));
        this.petriNet = petriNet;
    }

    @Override
    public String getMessage() {
        return "ProcessDeleteEvent: PetriNet [%s] deleted".formatted(petriNet.getIdentifier());
    }
}
