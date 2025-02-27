package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.authentication.domain.IUser;
import com.netgrif.application.engine.authentication.domain.LoggedUser;
import com.netgrif.application.engine.authorization.domain.Role;
import lombok.Getter;

import java.util.Collection;

@Getter
public class UserAssignRoleEvent extends UserEvent {

    protected final Collection<Role> roles;

    public UserAssignRoleEvent(LoggedUser user, Collection<Role> roles) {
        super(user);
        this.roles = roles;
    }

    public UserAssignRoleEvent(IUser user, Collection<Role> roles) {
        super(user.transformToLoggedUser());
        this.roles = roles;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Roles ");
        roles.forEach(role -> {
            sb.append(role.getTitleAsString());
            sb.append(",");
        });
        sb.append(" assigned to user ");
        sb.append(user.getUsername());
        return sb.toString();
    }
}