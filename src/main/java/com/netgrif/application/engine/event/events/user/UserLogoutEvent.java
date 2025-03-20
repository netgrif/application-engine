package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.utils.DateUtils;

public class UserLogoutEvent extends ActorEvent {

    public UserLogoutEvent(Identity user) {
        super(user);
    }

    @Override
    public String getMessage() {
        return "User " + user.getUsername() + " logged out on " + DateUtils.toString(time);
    }
}