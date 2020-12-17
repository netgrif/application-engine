package com.netgrif.workflow.petrinet.domain.events;
import lombok.Data;

@Data
public class Event extends BaseEvent {

    private EventType type;
}