package com.netgrif.workflow.petrinet.web.responsebodies;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;

import java.util.LinkedList;
import java.util.List;

public class UsersRolesListResponse {
    private List<User> users;
    private List<ProcessRole> roles;

    public UsersRolesListResponse() {
        this.users = new LinkedList<>();
        this.roles = new LinkedList<>();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<ProcessRole> getRoles() {
        return roles;
    }

    public void setRoles(List<ProcessRole> roles) {
        this.roles = roles;
    }
}
