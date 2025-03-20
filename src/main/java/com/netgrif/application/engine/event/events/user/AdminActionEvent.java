package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.authorization.domain.Actor;
import lombok.Getter;

@Getter
public class AdminActionEvent extends ActorEvent {

    private final String code;

    public AdminActionEvent(Actor actor, String code) {
        super(actor);
        this.code = code;
    }

    @Override
    public String getMessage() {
        return "Actor " + actor.getEmail() + " run following script: " + code;
    }
}
