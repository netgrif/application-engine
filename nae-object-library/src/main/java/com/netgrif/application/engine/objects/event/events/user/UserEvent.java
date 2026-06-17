package com.netgrif.application.engine.objects.event.events.user;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.event.events.Event;
import lombok.Getter;

@Getter
public abstract class UserEvent extends Event {

    protected final LoggedUser user;

    public UserEvent() {
        this(null);
    }

    public UserEvent(LoggedUser user) {
        super(user);
        this.user = user;
    }
}
