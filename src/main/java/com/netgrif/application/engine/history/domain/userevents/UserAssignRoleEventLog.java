package com.netgrif.application.engine.history.domain.userevents;

import com.netgrif.application.engine.authorization.domain.Role;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class UserAssignRoleEventLog extends UserEventLog implements IRolesEvent {

    private final List<Role> roles;

    public UserAssignRoleEventLog(String email, Collection<Role> roles) {
        super(email);
        this.roles = new ArrayList<>(roles);
    }
}
