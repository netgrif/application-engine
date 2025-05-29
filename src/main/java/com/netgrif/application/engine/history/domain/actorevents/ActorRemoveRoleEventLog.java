package com.netgrif.application.engine.history.domain.actorevents;

import com.netgrif.application.engine.authorization.domain.Role;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class ActorRemoveRoleEventLog extends ActorEventLog implements IRolesEvent {

    private final List<Role> roles;

    public ActorRemoveRoleEventLog(String email, Collection<Role> roles) {
        super(email);
        this.roles = new ArrayList<>(roles);
    }
}
