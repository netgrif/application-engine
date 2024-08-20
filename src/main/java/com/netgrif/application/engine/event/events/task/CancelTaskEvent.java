package com.netgrif.application.engine.event.events.task;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;

public class CancelTaskEvent extends TaskEvent {

    public CancelTaskEvent(CancelTaskEventOutcome eventOutcome) {
        super(eventOutcome);
    }

    @Override
    public String getMessage() {
        return "CancelTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] cancelled";
    }
}
