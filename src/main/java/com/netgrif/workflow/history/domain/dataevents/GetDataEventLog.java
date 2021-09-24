package com.netgrif.workflow.history.domain.dataevents;

import com.netgrif.workflow.history.domain.taskevents.TaskEventLog;
import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;

public class GetDataEventLog extends TaskEventLog {

    public GetDataEventLog(Task task, Case useCase, EventPhase eventPhase) {
        super(task, useCase, eventPhase);
    }
}
