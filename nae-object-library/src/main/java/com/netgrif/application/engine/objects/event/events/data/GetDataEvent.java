package com.netgrif.application.engine.objects.event.events.data;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import lombok.Getter;

@Getter
public class GetDataEvent extends DataEvent {

    protected GetDataEventOutcome eventOutcome;

    public GetDataEvent(GetDataEventOutcome eventOutcome) {
        super(eventOutcome);
        this.eventOutcome = eventOutcome;
    }

    public GetDataEvent(GetDataEventOutcome eventOutcome, AbstractUser user) {
        super(eventOutcome, user);
        this.eventOutcome = eventOutcome;
    }

    public GetDataEvent(GetDataEventOutcome eventOutcome, EventPhase eventPhase, AbstractUser user) {
        super(eventOutcome, eventPhase, user);
        this.eventOutcome = eventOutcome;
    }

    @Override
    public String getMessage() {
        return "GetDataEvent: GET [" + (eventOutcome.getMessage() == null ? MISSING_IDENTIFIER : eventOutcome.getMessage().toString()) + "]";
    }
}
