package com.netgrif.application.engine.event.events.task;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Getter;


@Getter
public abstract class TaskEvent extends Event {

    protected final TaskEventOutcome taskEventOutcome;

    public TaskEvent(TaskEventOutcome eventOutcome) {
        super(eventOutcome);
        this.taskEventOutcome = eventOutcome;
    }
}
