package com.netgrif.application.engine.objects.event.events.task;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;

public class CreateTaskEvent extends TaskEvent {

    public CreateTaskEvent(TaskEventOutcome eventOutcome, EventPhase eventPhase) {
        super(eventOutcome, eventPhase);
    }

    public CreateTaskEvent(TaskEventOutcome eventOutcome, EventPhase eventPhase, AbstractUser user) {
        super(eventOutcome, eventPhase, user);
    }

    @Override
    public String getMessage() {
        return "CreateTaskEvent: Task [%s] created"
                .formatted(taskEventOutcome.getTask() == null ? MISSING_IDENTIFIER : taskEventOutcome.getTask().getStringId());
    }

    @Override
    public EventType getEventType() {
        return null;
    }
}
