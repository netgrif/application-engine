package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.event.events.Event;
import lombok.Getter;

public abstract class UserEvent extends Event {

    @Getter
    protected final Identity user;

    public UserEvent(Identity user) {
        super(user);
        this.user = user;
    }
}