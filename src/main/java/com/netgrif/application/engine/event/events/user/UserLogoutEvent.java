package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.utils.DateUtils;

public class UserLogoutEvent extends ActorEvent {


    public UserLogoutEvent(Actor actor) {
        super(actor);
    }

    @Override
    public String getMessage() {
        // todo 2058
//        return "User " + user.getUsername() + " logged out on " + DateUtils.toString(time);
        return "mssg";
    }
}