package com.netgrif.workflow.petrinet.web.responsebodies;

import com.netgrif.workflow.auth.domain.IUser;

import java.util.LinkedList;
import java.util.List;

public class ProcessRolesUsersListResponse {
    List<IUser> users;

    public ProcessRolesUsersListResponse() {
        users = new LinkedList<>();
    }

    public List<IUser> getUsers() {
        return users;
    }

    public void setUsers(List<IUser> users) {
        this.users = users;
    }
}
