package com.netgrif.workflow.history.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
public abstract class EventLog {

    @Id
    @Getter
    protected ObjectId id;

    @Getter
    protected LocalDateTime created;

    @Getter @Setter
    protected String message;

    public EventLog() {
        this.id = new ObjectId();
    }

    public String getStringId() {
        return id.toString();
    }
}