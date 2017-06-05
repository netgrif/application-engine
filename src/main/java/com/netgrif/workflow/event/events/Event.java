package com.netgrif.workflow.event.events;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

public class Event extends ApplicationEvent {

    protected LocalDateTime time;

    public Event(Object source) {
        super(source);
        this.time = LocalDateTime.now();
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}