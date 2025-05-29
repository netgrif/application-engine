package com.netgrif.application.engine.event.events.authentication;

import com.netgrif.application.engine.authentication.domain.Identity;

public class IdentityLoginEvent extends IdentityEvent {

    // todo: release/8.0.0 needed? Spring security publishes AuthenticationSuccessEvent

    public IdentityLoginEvent(Identity identity) {
        super(identity);
    }

    @Override
    public String getMessage() {
        return "";
    }
}