package com.netgrif.application.engine.history.domain.dataevents;

import com.netgrif.application.engine.history.domain.taskevents.TaskEventLog;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import com.querydsl.core.annotations.QueryExclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@QueryExclude
@Document(collection = "eventLogs")
@EqualsAndHashCode(callSuper = true)
public class SetDataEventLog extends TaskEventLog {

    // TODO: release/8.0.0 final?
    @Getter
    private DataSet changedFields;

    public SetDataEventLog() {
        super();
    }

    public SetDataEventLog(Task task, Case useCase, EventPhase eventPhase, DataSet changedFields, String actorId) {
        super(task, useCase, eventPhase, actorId, null);
        this.changedFields = changedFields;
    }
}
