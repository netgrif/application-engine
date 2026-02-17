package com.netgrif.application.engine.objects.event.events.data;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import lombok.Getter;

@Getter
public class SetDataEvent extends DataEvent {

    protected SetDataEventOutcome eventOutcome;

    public SetDataEvent(SetDataEventOutcome eventOutcome) {
        this(eventOutcome, null, null);
    }

    public SetDataEvent(SetDataEventOutcome eventOutcome, AbstractUser user) {
        this(eventOutcome, null, user);
    }

    public SetDataEvent(SetDataEventOutcome eventOutcome, EventPhase eventPhase, AbstractUser user) {
        super(eventOutcome, eventPhase, user, getWorkspaceIdFromResource(eventOutcome.getCase()));
        this.eventOutcome = eventOutcome;
    }

    @Override
    public String getMessage() {
        return "SetDataEvent: SET [" + (eventOutcome.getMessage() == null ? MISSING_IDENTIFIER : eventOutcome.getMessage().toString()) + "]";
    }
}
