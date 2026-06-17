package com.netgrif.application.engine.objects.event.events.user;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.utils.DateUtils;

public class UserLoginEvent extends UserEvent {

    public UserLoginEvent(LoggedUser user) {
        super(user);
    }

    @Override
    public String getMessage() {
        return "User " +  (user.getUsername() == null ? MISSING_IDENTIFIER : user.getUsername())  + " logged in on "
                + DateUtils.toString(time);
    }
}
