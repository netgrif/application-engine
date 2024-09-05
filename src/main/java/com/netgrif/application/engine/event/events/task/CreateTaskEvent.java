package com.netgrif.application.engine.event.events.task;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.events.EventType;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;

public class CreateTaskEvent extends TaskEvent {

    public CreateTaskEvent(TaskEventOutcome eventOutcome, EventPhase eventPhase) {
        super(eventOutcome, eventPhase);
    }

    @Override
    public String getMessage() {
        return "CreateTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] created";
    }

    @Override
    public EventType getEventType() {
        return null;
    }
}
