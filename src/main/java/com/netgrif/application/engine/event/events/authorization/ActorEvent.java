package com.netgrif.application.engine.event.events.authorization;

import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.event.events.Event;
import lombok.Getter;

@Getter
public abstract class ActorEvent extends Event {

    protected final User actor;

    public ActorEvent(User actor) {
        super(actor);
        this.actor = actor;
    }
}