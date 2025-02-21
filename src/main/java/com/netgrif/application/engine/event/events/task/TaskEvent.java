package com.netgrif.application.engine.event.events.task;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.events.EventType;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Getter;


@Getter
public abstract class TaskEvent extends Event {

    protected final TaskEventOutcome taskEventOutcome;

    private LoggedUser user;

    public TaskEvent(TaskEventOutcome eventOutcome, EventPhase phase) {
        super(eventOutcome, phase);
        this.taskEventOutcome = eventOutcome;
    }

    public TaskEvent(TaskEventOutcome eventOutcome, IUser user) {
        super(eventOutcome);
        this.taskEventOutcome = eventOutcome;
        this.user = user.transformToLoggedUser();
    }

    public TaskEvent(TaskEventOutcome eventOutcome, EventPhase phase, IUser user) {
        super(eventOutcome, phase);
        this.taskEventOutcome = eventOutcome;
        this.user = user.transformToLoggedUser();
    }


    public abstract EventType getEventType();

}
