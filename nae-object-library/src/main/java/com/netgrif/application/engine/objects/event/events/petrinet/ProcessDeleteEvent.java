package com.netgrif.application.engine.objects.event.events.petrinet;

import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import lombok.Getter;

@Getter
public class ProcessDeleteEvent extends ProcessEvent {

    protected PetriNet petriNet;

    public ProcessDeleteEvent(PetriNet petriNet, EventPhase phase) {
        super(petriNet, phase);
        this.petriNet = petriNet;
    }

    @Override
    public String getMessage() {
        return "ProcessDeleteEvent: PetriNet [" + petriNet.getIdentifier() + "] deleted";
    }
}
