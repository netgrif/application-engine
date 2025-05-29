package com.netgrif.application.engine.event.events.authentication;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.utils.DateUtils;

public class IdentityRegistrationEvent extends IdentityEvent {

    public IdentityRegistrationEvent(Identity identity) {
        super(identity);
    }

    @Override
    public String getMessage() {
        return String.format("New identity %s registered on %s", identity.getUsername(), DateUtils.toString(time));
    }
}