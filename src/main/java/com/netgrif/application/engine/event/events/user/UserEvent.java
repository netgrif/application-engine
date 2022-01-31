package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.event.events.Event;
import lombok.Getter;

public abstract class UserEvent extends Event {

    @Getter
    protected final LoggedUser user;

    public UserEvent(LoggedUser user) {
        super(user);
        this.user = user;
    }
}