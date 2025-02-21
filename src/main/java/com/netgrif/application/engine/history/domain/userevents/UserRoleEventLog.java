package com.netgrif.application.engine.history.domain.userevents;

import com.netgrif.application.engine.authorization.domain.ProcessRole;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class UserRoleEventLog extends UserEventLog implements IRolesEvent {

    private final List<ProcessRole> processRoles;

    public UserRoleEventLog(String email, Collection<ProcessRole> processRoles) {
        super(email);
        this.processRoles = new ArrayList<>(processRoles);
    }
}
