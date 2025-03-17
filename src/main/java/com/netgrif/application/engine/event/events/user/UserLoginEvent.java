package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.utils.DateUtils;

public class UserLoginEvent extends UserEvent {

    public UserLoginEvent(Identity user) {
        super(user);
    }

    @Override
    public String getMessage() {
        return "User " + user.getUsername() + " logged in on " + DateUtils.toString(time);
    }
}