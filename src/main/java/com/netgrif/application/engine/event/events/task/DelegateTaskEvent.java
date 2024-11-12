package com.netgrif.application.engine.event.events.task;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.events.EventType;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.DelegateTaskEventOutcome;

public class DelegateTaskEvent extends TaskEvent {

    public DelegateTaskEvent(DelegateTaskEventOutcome eventOutcome, EventPhase eventPhase) {
        super(eventOutcome, eventPhase);
    }

    @Override
    public String getMessage() {
        return "DelegateTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] delegated";
    }
    @Override
    public EventType getEventType() {
        return EventType.DELEGATE;
    }
}
