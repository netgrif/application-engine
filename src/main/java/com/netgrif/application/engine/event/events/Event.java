package com.netgrif.application.engine.event.events;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.time.LocalDateTime;

public abstract class Event extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = -9102232475981679124L;
    @Getter
    private EventPhase eventPhase;
    @Getter
    protected LocalDateTime time;

    public Event(Object source) {
        super(source);
        this.time = LocalDateTime.now();
    }

    public Event(Object source, EventPhase eventPhase) {
        super(source);
        this.eventPhase = eventPhase;
    }

    public abstract String getMessage();
}