package com.netgrif.application.engine.history.domain.taskevents;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventLogs")
@EqualsAndHashCode(callSuper = true)
public class AssignTaskEventLog extends TaskEventLog {


    public AssignTaskEventLog() {
        super();
    }

    public AssignTaskEventLog(Task task, Case useCase, EventPhase eventPhase, String actorId) {
        super(task, useCase, eventPhase, actorId, null);
    }
}
