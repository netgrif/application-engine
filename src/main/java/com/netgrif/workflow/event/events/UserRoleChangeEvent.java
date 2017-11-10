package com.netgrif.workflow.event.events;

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

    public UserRoleChangeEvent(User user, Collection<ProcessRole> roles) {
        super(new LoggedUser(user.getId(), user.getEmail(), user.getPassword(), user.getAuthorities()));
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