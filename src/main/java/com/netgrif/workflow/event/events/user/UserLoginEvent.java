package com.netgrif.workflow.event.events.user;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.utils.DateUtils;

public class UserLoginEvent extends UserEvent {

    public UserLoginEvent(LoggedUser user) {
        super(user);
    }

    @Override
    public String getMessage() {
        return "User " + user.getUsername() + " logged in on " + DateUtils.toString(time);
    }
}