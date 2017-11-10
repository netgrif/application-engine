package com.netgrif.workflow.event.events;

import com.netgrif.workflow.auth.domain.LoggedUser;
import lombok.Getter;

public abstract class UserEvent extends Event {

    @Getter
    protected final LoggedUser user;

    public UserEvent(LoggedUser user) {
        super(user);
        this.user = user;
    }
}