package com.netgrif.workflow.event.events.user;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.utils.DateUtils;

public class UserRegistrationEvent extends UserEvent {

    public UserRegistrationEvent(User user) {
        super(new LoggedUser(
                user.get_id(),
                user.getEmail(),
                user.getPassword(),
                user.getAuthorities()
        ));
    }

    public UserRegistrationEvent(LoggedUser user) {
        super(user);
    }

    @Override
    public String getMessage() {
        return "New user " + user.getUsername() + " registered on " + DateUtils.toString(time);
    }
}