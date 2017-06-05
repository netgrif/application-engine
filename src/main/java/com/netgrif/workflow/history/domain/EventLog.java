package com.netgrif.workflow.history.domain;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
public class EventLog {

    @Id
    protected ObjectId id;

    protected LocalDateTime created;

    protected String message;

    public EventLog() {
        this.id = new ObjectId();
    }

    public ObjectId getId() {
        return id;
    }

    public String getStringId() {
        return id.toString();
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}