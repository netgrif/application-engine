package com.netgrif.workflow.event.events.user;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.event.events.Event;
import lombok.Getter;

public abstract class UserEvent extends Event {

    @Getter
    protected final LoggedUser user;

    public UserEvent(LoggedUser user) {
        super(user);
        this.user = user;
    }
}