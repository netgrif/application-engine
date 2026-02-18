package com.netgrif.application.engine.objects.event.events.petrinet;

import com.netgrif.application.engine.objects.event.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;

public abstract class ProcessEvent extends Event {
    protected ProcessEvent(Object source, String workspaceId) {
        super(source, workspaceId);
    }

    protected ProcessEvent(Object source, EventPhase eventPhase, String workspaceId) {
        super(source, eventPhase, workspaceId);
    }
}
