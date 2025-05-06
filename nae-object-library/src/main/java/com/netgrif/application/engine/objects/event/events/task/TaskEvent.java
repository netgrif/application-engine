package com.netgrif.application.engine.objects.event.events.task;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.event.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Getter;


@Getter
public abstract class TaskEvent extends Event {

    protected final TaskEventOutcome taskEventOutcome;

    private AbstractUser user;

    public TaskEvent(TaskEventOutcome eventOutcome, EventPhase phase) {
        super(eventOutcome, phase);
        this.taskEventOutcome = eventOutcome;
    }

    public TaskEvent(TaskEventOutcome eventOutcome, AbstractUser user) {
        super(eventOutcome);
        this.taskEventOutcome = eventOutcome;
        this.user = user;
    }

    public TaskEvent(TaskEventOutcome eventOutcome, EventPhase phase, AbstractUser user) {
        super(eventOutcome, phase);
        this.taskEventOutcome = eventOutcome;
        this.user = user;
    }


    public abstract EventType getEventType();

}
