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

    private final AbstractUser user;

    protected TaskEvent(TaskEventOutcome eventOutcome, EventPhase phase) {
        this(eventOutcome, phase, null);
    }

    protected TaskEvent(TaskEventOutcome eventOutcome, AbstractUser user) {
        this(eventOutcome, null, user);
    }

    protected TaskEvent(TaskEventOutcome eventOutcome, EventPhase phase, AbstractUser user) {
        super(eventOutcome, phase, getWorkspaceIdFromResource(eventOutcome.getCase()));
        this.taskEventOutcome = eventOutcome;
        this.user = user;
    }

    public abstract EventType getEventType();

}
