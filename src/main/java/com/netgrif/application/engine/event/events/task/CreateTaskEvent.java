package com.netgrif.application.engine.event.events.task;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;

public class CreateTaskEvent  extends TaskEvent {

    public CreateTaskEvent(TaskEventOutcome eventOutcome) {
        super(eventOutcome);
    }

    @Override
    public String getMessage() {
        return "CreateTaskEvent: Task [" + taskEventOutcome.getTask().getStringId() + "] created";
    }
}
