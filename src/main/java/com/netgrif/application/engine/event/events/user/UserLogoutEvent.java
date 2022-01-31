package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.utils.DateUtils;

public class UserLogoutEvent extends UserEvent {

    public UserLogoutEvent(LoggedUser user) {
        super(user);
    }

    @Override
    public String getMessage() {
        return "User " + user.getUsername() + " logged out on " + DateUtils.toString(time);
    }
}