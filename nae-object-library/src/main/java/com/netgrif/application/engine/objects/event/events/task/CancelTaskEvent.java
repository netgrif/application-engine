package com.netgrif.application.engine.objects.event.events.task;

import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;

public class CancelTaskEvent extends TaskEvent {

    public CancelTaskEvent(CancelTaskEventOutcome eventOutcome, EventPhase eventPhase) {
        super(eventOutcome, eventPhase);
    }

    public CancelTaskEvent(CancelTaskEventOutcome eventOutcome, IUser user) {
        super(eventOutcome, user);
    }

    public CancelTaskEvent(CancelTaskEventOutcome outcome, EventPhase eventPhase, IUser user) {
        super(outcome, eventPhase, user);
    }

    @Override
    public String getMessage() {
        return "CancelTaskEvent: Task [" + (taskEventOutcome.getTask() == null ? MISSING_IDENTIFIER : taskEventOutcome.getTask().getStringId()) + "] cancelled";
    }
    @Override
    public EventType getEventType() {
        return EventType.CANCEL;
    }
}
