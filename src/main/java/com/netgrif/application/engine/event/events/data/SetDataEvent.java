package com.netgrif.application.engine.event.events.data;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import lombok.Getter;

@Getter
public class SetDataEvent extends DataEvent {

    protected SetDataEventOutcome eventOutcome;

    public SetDataEvent(SetDataEventOutcome eventOutcome) {
        super(eventOutcome);
        this.eventOutcome = eventOutcome;
    }

    public SetDataEvent(SetDataEventOutcome eventOutcome, IUser user) {
        super(eventOutcome, user);
        this.eventOutcome = eventOutcome;
    }

    public SetDataEvent(SetDataEventOutcome eventOutcome, EventPhase eventPhase, IUser user) {
        super(eventOutcome, eventPhase, user);
        this.eventOutcome = eventOutcome;
    }

    @Override
    public String getMessage() {
        return "SetDataEvent: SET [" + eventOutcome.getMessage()  + "]";
    }
}
