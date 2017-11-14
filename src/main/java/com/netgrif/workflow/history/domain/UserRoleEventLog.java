package com.netgrif.workflow.history.domain;

import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Document
public class UserRoleEventLog extends UserEventLog implements IProcessRolesEvent {

    private List<ProcessRole> roles;

    public UserRoleEventLog(String email, Collection<ProcessRole> roles) {
        super(email);
        setProcessRoles(roles);
    }

    @Override
    public void setProcessRoles(Collection<ProcessRole> roles) {
        this.roles = new LinkedList<>(roles);
    }

    @Override
    public Collection<ProcessRole> getProcessRoles() {
        return roles;
    }
}