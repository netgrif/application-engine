package com.netgrif.application.engine.objects.event.events.task;

import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;

public class AssignTaskEvent extends TaskEvent {

    public AssignTaskEvent(AssignTaskEventOutcome eventOutcome, EventPhase eventPhase) {
        super(eventOutcome, eventPhase);
    }

    public AssignTaskEvent(AssignTaskEventOutcome eventOutcome, EventPhase eventPhase, IUser user) {
        super(eventOutcome, eventPhase, user);
    }

    public AssignTaskEvent(AssignTaskEventOutcome eventOutcome, IUser user) {
        super(eventOutcome, user);
    }

    @Override
    public String getMessage() {
        return "AssignTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] assigned";
    }

    @Override
    public EventType getEventType() {
        return EventType.ASSIGN;
    }
}
