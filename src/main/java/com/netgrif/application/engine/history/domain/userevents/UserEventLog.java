package com.netgrif.application.engine.history.domain.userevents;

import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import lombok.Getter;

public class UserEventLog extends EventLog implements IUserEventLog {

    @Getter
    private final String email;

    public UserEventLog(String email) {
        this.email = email;
    }
}
