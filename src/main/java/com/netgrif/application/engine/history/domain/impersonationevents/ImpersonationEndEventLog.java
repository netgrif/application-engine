package com.netgrif.application.engine.history.domain.impersonationevents;

import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventLogs")
@EqualsAndHashCode(callSuper = true)
public class ImpersonationEndEventLog extends ImpersonationEventLog {

    public ImpersonationEndEventLog() {
        super();
    }

    public ImpersonationEndEventLog(String impersonator, String impersonated) {
        super(impersonator, impersonated);
    }
}
