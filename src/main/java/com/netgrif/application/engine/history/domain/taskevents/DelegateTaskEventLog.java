package com.netgrif.application.engine.history.domain.taskevents;

import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventLogs")
@EqualsAndHashCode(callSuper = true)
public class DelegateTaskEventLog extends TaskEventLog {
    public DelegateTaskEventLog() {
        super();
    }
}
