package com.netgrif.workflow.history.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserEventLog extends EventLog {

    @Getter @Setter
    protected String email;

    public UserEventLog() {
        super();
    }

    public UserEventLog(String email, String message) {
        this();
        this.email = email;
        this.message = message;
    }
}