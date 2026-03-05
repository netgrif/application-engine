package com.netgrif.application.engine.objects.event.events.user;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.utils.DateUtils;

public class UserRegistrationEvent extends UserEvent {

    public UserRegistrationEvent(LoggedUser user) {
        super(user, null);
    }

    @Override
    public String getMessage() {
        return "New user %s registered on %s".formatted(getActor().getUsername() == null ? MISSING_IDENTIFIER : getActor().getUsername(),
                DateUtils.toString(time));
    }
}
