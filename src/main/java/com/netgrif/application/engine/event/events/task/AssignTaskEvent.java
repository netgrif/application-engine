package com.netgrif.application.engine.event.events.task;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;

public class AssignTaskEvent extends TaskEvent {

    public AssignTaskEvent(AssignTaskEventOutcome eventOutcome, EventPhase eventPhase) {
        super(eventOutcome, eventPhase);
    }

    @Override
    public String getMessage() {
        return "AssignTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] assigned";
    }
}
