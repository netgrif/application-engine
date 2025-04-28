package com.netgrif.application.engine.event.events.authentication;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authorization.domain.Actor;

public class IdentityLoginEvent extends IdentityEvent {

    // todo 2058 needed? Spring security publishes AuthenticationSuccessEvent

    public IdentityLoginEvent(Identity identity) {
        super(identity);
    }

    @Override
    public String getMessage() {
        return "";
    }
}