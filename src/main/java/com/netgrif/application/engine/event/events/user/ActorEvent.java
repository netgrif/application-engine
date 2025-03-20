package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.event.events.Event;
import lombok.Getter;

@Getter
public abstract class ActorEvent extends Event {

    protected final Actor actor;

    public ActorEvent(Actor actor) {
        super(actor);
        this.actor = actor;
    }
}