package com.netgrif.application.engine.history.domain.processevents;

import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import com.netgrif.application.engine.workflow.domain.events.EventPhase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public abstract class ProcessEventLog extends EventLog {

    protected ObjectId templateCaseId;

    protected ProcessEventLog() {
        super();
    }

    protected ProcessEventLog(ObjectId triggerId, EventPhase eventPhase, ObjectId templateCaseId) {
        super(triggerId, eventPhase);
        this.templateCaseId = templateCaseId;
    }

    protected ProcessEventLog(ObjectId triggerId, EventPhase eventPhase, List<ObjectId> triggeredEvents, ObjectId templateCaseId) {
        super(triggerId, eventPhase, triggeredEvents);
        this.templateCaseId = templateCaseId;
    }
}
