package com.netgrif.workflow.petrinet.web.responsebodies;

import com.netgrif.workflow.auth.domain.User;

import java.util.LinkedList;
import java.util.List;

public class ProcessRolesUsersListResponse {
    List<User> users;

    public ProcessRolesUsersListResponse() {
        users = new LinkedList<>();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
