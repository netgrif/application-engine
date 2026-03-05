package com.netgrif.application.engine.objects.event.events.data;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.event.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import lombok.Getter;

@Getter
public abstract class DataEvent extends Event {

    protected DataEvent(Object source, String workspaceId) {
        super(source, null, workspaceId);
    }

    protected DataEvent(Object source, AbstractUser user, String workspaceId) {
        this(source, null, user, workspaceId);
    }

    protected DataEvent(Object source, EventPhase eventPhase, AbstractUser user, String workspaceId) {
        super(source, eventPhase, ActorTransformer.toActorRef(user), workspaceId);
    }

}
