package com.netgrif.application.engine.objects.event.events.user;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.utils.DateUtils;

public class UserRegistrationEvent extends UserEvent {

    public UserRegistrationEvent(LoggedUser user) {
        super(user);
    }


    @Override
    public String getMessage() {
        return "New user " + user.getUsername() + " registered on " + DateUtils.toString(time);
    }
}
