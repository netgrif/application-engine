package com.netgrif.application.engine.event.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public abstract class Event extends ApplicationEvent {

    protected LocalDateTime time;

    public Event(Object source) {
        super(source);
        this.time = LocalDateTime.now();
    }

    public abstract String getMessage();
}