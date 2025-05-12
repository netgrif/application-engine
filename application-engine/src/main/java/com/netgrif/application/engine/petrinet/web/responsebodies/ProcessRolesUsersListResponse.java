package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;

import java.util.LinkedList;
import java.util.List;

public class ProcessRolesUsersListResponse {
    List<AbstractUser> users;

    public ProcessRolesUsersListResponse() {
        users = new LinkedList<>();
    }

    public List<AbstractUser> getUsers() {
        return users;
    }

    public void setUsers(List<AbstractUser> users) {
        this.users = users;
    }
}
