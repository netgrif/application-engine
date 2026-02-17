package com.netgrif.application.engine.objects.event.events.user;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.utils.DateUtils;

public class UserLogoutEvent extends UserEvent {

    public UserLogoutEvent(LoggedUser user) {
        super(user, null);
    }

    @Override
    public String getMessage() {
        return "User %s logged out on %s".formatted(user.getUsername() == null ? MISSING_IDENTIFIER : user.getUsername(),
                DateUtils.toString(time));
    }
}
