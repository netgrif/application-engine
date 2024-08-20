package com.netgrif.application.engine.event.events.petrinet;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import lombok.Getter;

@Getter
public class ProcessDeleteEvent extends Event {

    protected PetriNet petriNet;

    public ProcessDeleteEvent(PetriNet petriNet) {
        super(petriNet);
        this.petriNet = petriNet;
    }

    @Override
    public String getMessage() {
        return "ProcessDeleteEvent: PetriNet [" + petriNet.getIdentifier() + "] deleted";
    }
}
