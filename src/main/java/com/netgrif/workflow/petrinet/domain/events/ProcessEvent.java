package com.netgrif.workflow.petrinet.domain.events;

import lombok.Data;

@Data
public class ProcessEvent extends BaseEvent {

    private ProcessEventType type;
}

