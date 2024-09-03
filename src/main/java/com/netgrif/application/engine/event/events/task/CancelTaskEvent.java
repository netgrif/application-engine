package com.netgrif.application.engine.event.events.task;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;

public class CancelTaskEvent extends TaskEvent {

    public CancelTaskEvent(CancelTaskEventOutcome eventOutcome, EventPhase eventPhase) {
        super(eventOutcome, eventPhase);
    }

    @Override
    public String getMessage() {
        return "CancelTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] cancelled";
    }
}
