package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import lombok.Getter;

import java.util.Collection;

public class UserRoleChangeEvent extends UserEvent {

    @Getter
    protected final Collection<ProcessRole> roles;

    public UserRoleChangeEvent(LoggedUser user, Collection<ProcessRole> roles) {
        super(user);
        this.roles = roles;
    }

    public UserRoleChangeEvent(IUser user, Collection<ProcessRole> roles) {
        super(user.transformToLoggedUser());
        this.roles = roles;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Roles ");
        roles.forEach(role -> {
            sb.append(role.getName());
            sb.append(",");
        });
        sb.append(" assigned to user ");
        sb.append(user.getUsername());
        return sb.toString();
    }
}