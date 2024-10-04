package com.netgrif.application.engine.event.events.data;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import lombok.Getter;

@Getter
public class GetDataEvent extends Event {

    protected GetDataEventOutcome eventOutcome;

    public GetDataEvent(GetDataEventOutcome eventOutcome) {
        super(eventOutcome);
        this.eventOutcome = eventOutcome;
    }

    @Override
    public String getMessage() {
        return "GetDataEvent: GET [" + eventOutcome.getMessage().toString() + "]";
    }
}
