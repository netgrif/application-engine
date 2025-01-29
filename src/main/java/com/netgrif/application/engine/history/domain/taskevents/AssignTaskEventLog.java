package com.netgrif.application.engine.history.domain.taskevents;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.event.events.data.GetDataEvent;
import com.netgrif.application.engine.event.events.petrinet.ProcessDeleteEvent;
import com.netgrif.application.engine.event.events.task.AssignTaskEvent;
import com.netgrif.application.engine.history.domain.dataevents.GetDataEventLog;
import com.netgrif.application.engine.history.domain.petrinetevents.DeletePetriNetEventLog;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
public class AssignTaskEventLog extends TaskEventLog {


    public AssignTaskEventLog() {
        super();
    }

    public AssignTaskEventLog(Task task, Case useCase, EventPhase eventPhase, IUser user) {
        super(task, useCase, eventPhase, user.getStringId(), user.isImpersonating() ? user.getImpersonated().getStringId() : null);
    }

    public static AssignTaskEventLog fromEvent(AssignTaskEvent event) {
        return new AssignTaskEventLog(event.getTaskEventOutcome().getTask(), event.getTaskEventOutcome().getCase(), event.getEventPhase(), event.getUser());
    }
}
