package com.netgrif.workflow.history.domain;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserEventLog extends EventLog {

    private String email;

    public UserEventLog() {
        super();
    }

    public UserEventLog(String email, String message) {
        this();
        this.email = email;
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}