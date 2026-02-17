package com.netgrif.application.engine.objects.event.events.task;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;

public class AssignTaskEvent extends TaskEvent {

    public AssignTaskEvent(AssignTaskEventOutcome eventOutcome, EventPhase eventPhase) {
        super(eventOutcome, eventPhase);
    }

    public AssignTaskEvent(AssignTaskEventOutcome eventOutcome, EventPhase eventPhase, AbstractUser user) {
        super(eventOutcome, eventPhase, user);
    }

    public AssignTaskEvent(AssignTaskEventOutcome eventOutcome, AbstractUser user) {
        super(eventOutcome, user);
    }

    @Override
    public String getMessage() {
        return "AssignTaskEvent: Task [%s] assigned"
                .formatted(taskEventOutcome.getTask() == null ? MISSING_IDENTIFIER : taskEventOutcome.getTask().getStringId());
    }

    @Override
    public EventType getEventType() {
        return EventType.ASSIGN;
    }
}
