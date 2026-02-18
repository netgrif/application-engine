package com.netgrif.application.engine.objects.event.events.data;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import lombok.Getter;

@Getter
public class GetDataEvent extends DataEvent {

    protected GetDataEventOutcome eventOutcome;

    public GetDataEvent(GetDataEventOutcome eventOutcome) {
        this(eventOutcome, null, null);
    }

    public GetDataEvent(GetDataEventOutcome eventOutcome, AbstractUser user) {
        this(eventOutcome, null, user);
    }

    public GetDataEvent(GetDataEventOutcome eventOutcome, EventPhase eventPhase, AbstractUser user) {
        super(eventOutcome, eventPhase, user, getWorkspaceIdFromResource(eventOutcome.getCase()));
        this.eventOutcome = eventOutcome;
    }

    @Override
    public String getMessage() {
        return "GetDataEvent: GET [%s]".formatted(eventOutcome.getMessage() == null ? MISSING_IDENTIFIER : eventOutcome.getMessage().toString());
    }
}
