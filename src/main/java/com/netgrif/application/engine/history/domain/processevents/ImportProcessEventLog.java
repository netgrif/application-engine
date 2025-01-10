package com.netgrif.application.engine.history.domain.processevents;

import com.netgrif.application.engine.workflow.domain.events.EventPhase;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventLogs")
@EqualsAndHashCode(callSuper = true)
public class ImportProcessEventLog extends ProcessEventLog {

    public ImportProcessEventLog(ObjectId triggerId, EventPhase eventPhase, ObjectId templateCaseId) {
        super(triggerId, eventPhase, templateCaseId);
    }
}
