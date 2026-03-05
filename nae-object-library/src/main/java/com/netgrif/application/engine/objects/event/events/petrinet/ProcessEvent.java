package com.netgrif.application.engine.objects.event.events.petrinet;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.event.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;

public abstract class ProcessEvent extends Event {
    protected ProcessEvent(Object source, AbstractUser user, String workspaceId) {
        super(source, ActorTransformer.toActorRef(user), workspaceId);
    }

    protected ProcessEvent(Object source, EventPhase eventPhase, AbstractUser user, String workspaceId) {
        super(source, eventPhase, ActorTransformer.toActorRef(user), workspaceId);
    }
}
