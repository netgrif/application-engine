package com.netgrif.application.engine.event.events.authentication;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.event.events.Event;
import lombok.Getter;

@Getter
public abstract class IdentityEvent extends Event {

    protected final Identity identity;

    public IdentityEvent(Identity identity) {
        super(identity);
        this.identity = identity;
    }
}
