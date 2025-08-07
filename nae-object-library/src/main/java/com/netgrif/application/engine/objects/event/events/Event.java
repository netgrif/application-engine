package com.netgrif.application.engine.objects.event.events;

import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import lombok.Getter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.EventObject;

@Getter
public abstract class Event extends EventObject {
    @Serial
    private static final long serialVersionUID = -9102232475981679124L;
    private EventPhase eventPhase;
    protected LocalDateTime time;

    protected static final String MISSING_IDENTIFIER = "NULL";

    public Event() {
        this(null);
    }

    public Event(Object source) {
        super(source);
        this.time = LocalDateTime.now();
    }

    public Event(Object source, EventPhase eventPhase) {
        super(source);
        this.eventPhase = eventPhase;
        this.time = LocalDateTime.now();
    }

    public abstract String getMessage();
}
