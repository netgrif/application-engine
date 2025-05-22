package com.netgrif.application.engine.event.events.authentication;

import com.netgrif.application.engine.authentication.domain.Identity;

public class IdentityLogoutEvent extends IdentityEvent {

    // todo: release/8.0.0 remove or keep (call as result of /logout ?)

    public IdentityLogoutEvent(Identity identity) {
        super(identity);
    }

    @Override
    public String getMessage() {
        return "";
    }
}