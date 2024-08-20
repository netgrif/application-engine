package com.netgrif.application.engine.event.events.task;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.DelegateTaskEventOutcome;

public class DelegateTaskEvent extends TaskEvent {

    public DelegateTaskEvent(DelegateTaskEventOutcome eventOutcome) {
        super(eventOutcome);
    }

    @Override
    public String getMessage() {
        return "DelegateTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] delegated";
    }
}
