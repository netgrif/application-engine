package com.netgrif.application.engine.history.domain.baseevent;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Document(collection = "eventLogs")
public abstract class EventLog {

    @Id
    protected ObjectId id;

    protected ObjectId triggerId;

    protected EventPhase eventPhase;

    @Setter
    protected List<ObjectId> triggeredEvents;

    protected EventLog() {
        this.id = new ObjectId();
    }

    protected EventLog(ObjectId triggerId, EventPhase eventPhase) {
        this();
        this.triggerId = triggerId;
        this.eventPhase = eventPhase;
    }

    protected EventLog(ObjectId triggerId, EventPhase eventPhase, List<ObjectId> triggeredEvents) {
        this(triggerId, eventPhase);
        this.triggeredEvents = triggeredEvents;
    }

    public String getStringId() {
        return id.toString();
    }
}
