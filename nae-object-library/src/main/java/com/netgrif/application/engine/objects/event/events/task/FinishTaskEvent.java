package com.netgrif.application.engine.objects.event.events.task;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;

public class FinishTaskEvent extends TaskEvent {

    public FinishTaskEvent(FinishTaskEventOutcome eventOutcome, EventPhase eventPhase) {
        super(eventOutcome, eventPhase);
    }

    public FinishTaskEvent(FinishTaskEventOutcome eventOutcome, AbstractUser user) {
        super(eventOutcome, user);
    }

    public FinishTaskEvent(FinishTaskEventOutcome eventOutcome, EventPhase eventPhase, AbstractUser user) {
        super(eventOutcome, eventPhase, user);
    }

    @Override
    public String getMessage() {
        return "FinishTaskEvent: Task [%s] finished"
                .formatted(taskEventOutcome.getTask() == null ? MISSING_IDENTIFIER : taskEventOutcome.getTask().getStringId());
    }

    @Override
    public EventType getEventType() {
        return EventType.FINISH;
    }
}
