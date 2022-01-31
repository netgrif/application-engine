package com.netgrif.workflow.event.events.user;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.utils.DateUtils;

public class UserLogoutEvent extends UserEvent {

    public UserLogoutEvent(LoggedUser user) {
        super(user);
    }

    @Override
    public String getMessage() {
        return "User " + user.getUsername() + " logged out on " + DateUtils.toString(time);
    }
}