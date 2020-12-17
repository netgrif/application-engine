package com.netgrif.workflow.petrinet.domain.events;

import lombok.Data;

@Data
public class CaseEvent extends BaseEvent {

    private CaseEventType type;
}