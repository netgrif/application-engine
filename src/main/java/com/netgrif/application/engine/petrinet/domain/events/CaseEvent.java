package com.netgrif.application.engine.petrinet.domain.events;

import lombok.Data;

@Data
public class CaseEvent extends BaseEvent {

    private CaseEventType type;
}