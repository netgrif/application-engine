package com.netgrif.application.engine.history.domain.processevents;

import com.netgrif.application.engine.workflow.domain.events.EventPhase;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventLogs")
@EqualsAndHashCode(callSuper = true)
public class DeleteProcessEventLog extends ProcessEventLog {

    public DeleteProcessEventLog(ObjectId triggerId, EventPhase eventPhase, ObjectId netId) {
        super(triggerId, eventPhase, netId);
    }
}
