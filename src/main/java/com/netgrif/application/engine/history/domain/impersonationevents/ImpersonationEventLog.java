package com.netgrif.application.engine.history.domain.impersonationevents;

import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public abstract class ImpersonationEventLog extends EventLog {

    @Getter
    private String impersonator;

    @Getter
    private String impersonated;

    public ImpersonationEventLog() {
        super();
    }

    public ImpersonationEventLog(String impersonator, String impersonated) {
        super();
        this.impersonator = impersonator;
        this.impersonated = impersonated;
    }
}
