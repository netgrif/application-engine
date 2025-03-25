package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.utils.DateUtils;

public class UserRegistrationEvent extends ActorEvent {

    public UserRegistrationEvent(RegisteredUser user) {
        super(new Identity(
                user.getStringId(),
                user.getEmail(),
                user.getPassword(),
                user.getAuthorities()
        ));
    }

    public UserRegistrationEvent(Identity user) {
        super(user);
    }

    public UserRegistrationEvent(IUser user) {
        super(new Identity(
                user.getStringId(),
                user.getEmail(),
                "",
                user.getAuthorities()
        ));
    }

    @Override
    public String getMessage() {
        return "New user " + user.getUsername() + " registered on " + DateUtils.toString(time);
    }
}