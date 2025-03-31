package com.netgrif.application.engine.history.domain.userevents;

import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import lombok.Getter;

@Getter
public class ActorEventLog extends EventLog implements IActorEventLog {

    private final String email;

    public ActorEventLog(String email) {
        this.email = email;
    }
}
