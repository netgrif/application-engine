package com.netgrif.workflow.history.domain.userevents;

import com.netgrif.workflow.history.domain.baseevent.EventLog;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserEventLog extends EventLog implements IUserEventLog {

    private String email;

    public UserEventLog(String email) {
        this.email = email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getEmail() {
        return email;
    }
}