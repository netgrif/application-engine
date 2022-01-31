package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.RegisteredUser;
import com.netgrif.application.engine.utils.DateUtils;

public class UserRegistrationEvent extends UserEvent {

    public UserRegistrationEvent(RegisteredUser user) {
        super(new LoggedUser(
                user.getStringId(),
                user.getEmail(),
                user.getPassword(),
                user.getAuthorities()
        ));
    }

    public UserRegistrationEvent(LoggedUser user) {
        super(user);
    }

    public UserRegistrationEvent(IUser user) {
        super(new LoggedUser(
                user.getStringId(),
                user.getEmail(),
                "",
                user.getAuthorities()
        ));
    }

    @Override
    public String getMessage() {
        return "New user " + user.getUsername() + " registered on " + DateUtils.toString(time);
    }
}