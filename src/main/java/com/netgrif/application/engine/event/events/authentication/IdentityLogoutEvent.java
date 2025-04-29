package com.netgrif.application.engine.event.events.authentication;

import com.netgrif.application.engine.authentication.domain.Identity;

public class IdentityLogoutEvent extends IdentityEvent {

    // todo 2058 remove or keep (call as result of /logout ?)

    public IdentityLogoutEvent(Identity identity) {
        super(identity);
    }

    @Override
    public String getMessage() {
        return "";
    }
}