package com.netgrif.application.engine.objects.event.events.user;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.utils.DateUtils;

public class UserLoginEvent extends UserEvent {

    public UserLoginEvent(LoggedUser user) {
        super(user, null);
    }

    @Override
    public String getMessage() {
        return "User %s logged in on %s".formatted(getActor().getUsername() == null ? MISSING_IDENTIFIER : getActor().getUsername(),
                DateUtils.toString(time));
    }
}
