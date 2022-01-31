package com.netgrif.application.engine.petrinet.domain.events;

import lombok.Data;

@Data
public class Event extends BaseEvent {

    private EventType type;
}