package com.netgrif.application.engine.objects.event.events.petrinet;

import com.netgrif.application.engine.objects.event.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;

public abstract class ProcessEvent extends Event {
    public ProcessEvent(Object source) {
        super(source);
    }

    public ProcessEvent(Object source, EventPhase eventPhase) {
        super(source, eventPhase);
    }
}
