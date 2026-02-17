package com.netgrif.application.engine.objects.event.events.user;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.event.events.Event;
import lombok.Getter;

@Getter
public abstract class UserEvent extends Event {

    protected final LoggedUser user;

    protected UserEvent() {
        this(null, null);
    }

    protected UserEvent(LoggedUser user, String workspaceId) {
        super(user, workspaceId);
        this.user = user;
    }
}
