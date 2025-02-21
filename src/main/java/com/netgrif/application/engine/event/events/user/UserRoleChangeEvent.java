package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import lombok.Getter;

import java.util.Collection;

public class UserRoleChangeEvent extends UserEvent {

    @Getter
    protected final Collection<ProcessRole> processRoles;

    public UserRoleChangeEvent(LoggedUser user, Collection<ProcessRole> processRoles) {
        super(user);
        this.processRoles = processRoles;
    }

    public UserRoleChangeEvent(IUser user, Collection<ProcessRole> processRoles) {
        super(user.transformToLoggedUser());
        this.processRoles = processRoles;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Roles ");
        processRoles.forEach(role -> {
            sb.append(role.getTitle());
            sb.append(",");
        });
        sb.append(" assigned to user ");
        sb.append(user.getUsername());
        return sb.toString();
    }
}