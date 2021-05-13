package com.netgrif.workflow.event.events.user;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
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