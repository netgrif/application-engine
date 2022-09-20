package com.netgrif.application.engine.history.domain.dataevents;

import com.netgrif.application.engine.history.domain.taskevents.TaskEventLog;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventLogs")
@EqualsAndHashCode(callSuper = true)
public class GetDataEventLog extends TaskEventLog {

    public GetDataEventLog() {
        super();
    }

    public GetDataEventLog(Task task, Case useCase, EventPhase eventPhase) {
        super(task, useCase, eventPhase);
    }
}
