package com.netgrif.application.engine.event.events.data;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import lombok.Getter;

@Getter
public class SetDataEvent extends Event {

    protected SetDataEventOutcome eventOutcome;

    public SetDataEvent(SetDataEventOutcome eventOutcome) {
        super(eventOutcome);
        this.eventOutcome = eventOutcome;
    }

    @Override
    public String getMessage() {
        return "SetDataEvent: SET [" + eventOutcome.getMessage().toString() + "]";
    }
}
