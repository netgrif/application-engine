package com.netgrif.workflow.event.events.user;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.event.events.user.UserEvent;
import com.netgrif.workflow.utils.DateUtils;

public class UserLogoutEvent extends UserEvent {

    public UserLogoutEvent(LoggedUser user) {
        super(user);
    }

    @Override
    public String getMessage() {
        return "Užívateľ " + user.getUsername() + " sa odhlásil o " + DateUtils.toString(time);
    }
}