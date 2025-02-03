package com.netgrif.application.engine.history.domain.taskevents;

import com.netgrif.core.auth.domain.IUser;
import com.netgrif.core.event.events.task.CreateTaskEvent;
import com.netgrif.core.event.events.task.FinishTaskEvent;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.adapter.workflow.domain.Case;
import com.netgrif.adapter.workflow.domain.Task;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;


@EqualsAndHashCode(callSuper = true)
public class FinishTaskEventLog extends TaskEventLog {

    public FinishTaskEventLog() {
        super();
    }

    public FinishTaskEventLog(Task task, Case useCase, EventPhase eventPhase, IUser user) {
        super(task, useCase, eventPhase, user.getStringId(), user.isImpersonating() ? user.getImpersonated().getStringId() : null);
    }

    public static FinishTaskEventLog fromEvent(FinishTaskEvent event) {
        return new FinishTaskEventLog(event.getTaskEventOutcome().getTask(), event.getTaskEventOutcome().getCase(), event.getEventPhase(), event.getUser());
    }
}
