package com.netgrif.application.engine.event.events.task;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;

public class FinishTaskEvent extends TaskEvent {

    public FinishTaskEvent(FinishTaskEventOutcome eventOutcome) {
        super(eventOutcome);
    }

    @Override
    public String getMessage() {
        return "FinishTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] finished";
    }
}
