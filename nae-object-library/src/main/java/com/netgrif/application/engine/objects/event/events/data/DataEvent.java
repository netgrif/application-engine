package com.netgrif.application.engine.objects.event.events.data;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.event.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import lombok.Getter;

@Getter
public abstract class DataEvent extends Event {

    private LoggedUser user;

    protected DataEvent(Object source, String workspaceId) {
        super(source, workspaceId);
    }

    protected DataEvent(Object source, AbstractUser user, String workspaceId) {
        this(source, null, user, workspaceId);
    }

    protected DataEvent(Object source, EventPhase eventPhase, AbstractUser user, String workspaceId) {
        super(source, eventPhase, workspaceId);
        if (user != null) {
            this.user = ActorTransformer.toLoggedUser(user);
        }
    }

}
