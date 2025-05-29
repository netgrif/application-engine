package com.netgrif.application.engine.history.domain.identityevents;

import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import lombok.Getter;

@Getter
public class IdentityEventLog extends EventLog implements IIdentityEventLog {

    private final String username;

    public IdentityEventLog(String username) {
        this.username = username;
    }
}
