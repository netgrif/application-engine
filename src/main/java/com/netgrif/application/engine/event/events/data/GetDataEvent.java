package com.netgrif.application.engine.event.events.data;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import lombok.Getter;

@Getter
public class GetDataEvent extends DataEvent {

    protected GetDataEventOutcome eventOutcome;

    public GetDataEvent(GetDataEventOutcome eventOutcome) {
        super(eventOutcome);
        this.eventOutcome = eventOutcome;
    }

    public GetDataEvent(GetDataEventOutcome eventOutcome, IUser user) {
        super(eventOutcome, user);
        this.eventOutcome = eventOutcome;
    }

    public GetDataEvent(GetDataEventOutcome eventOutcome, EventPhase eventPhase, IUser user) {
        super(eventOutcome,eventPhase, user);
        this.eventOutcome = eventOutcome;
    }

    @Override
    public String getMessage() {
        return "GetDataEvent: GET [" + eventOutcome.getMessage() + "]";
    }
}
