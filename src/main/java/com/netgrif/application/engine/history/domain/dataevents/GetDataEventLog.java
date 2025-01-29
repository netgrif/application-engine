package com.netgrif.application.engine.history.domain.dataevents;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.event.events.data.GetDataEvent;
import com.netgrif.application.engine.history.domain.taskevents.TaskEventLog;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;


@EqualsAndHashCode(callSuper = true)
public class GetDataEventLog extends TaskEventLog {

    public GetDataEventLog() {
        super();
    }

    public GetDataEventLog(Task task, Case useCase, EventPhase eventPhase, IUser user) {
        super(task, useCase, eventPhase, user.getStringId(), user.isImpersonating() ? user.getImpersonated().getStringId() : null);
    }

    public static GetDataEventLog fromEvent(GetDataEvent event) {
        return new GetDataEventLog(event.getEventOutcome().getTask(), event.getEventOutcome().getCase(), event.getEventPhase(), event.getUser());
    }
}
