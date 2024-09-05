package com.netgrif.application.engine.event.events.task;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.events.EventType;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Getter;


@Getter
public abstract class TaskEvent extends Event {

    protected final TaskEventOutcome taskEventOutcome;

    public TaskEvent(TaskEventOutcome eventOutcome, EventPhase phase) {
        super(eventOutcome, phase);
        this.taskEventOutcome = eventOutcome;
    }

    public abstract EventType getEventType();

}
