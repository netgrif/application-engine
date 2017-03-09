package com.fmworkflow.auth.web.responsebodies;

import com.fmworkflow.auth.domain.Role;
import com.fmworkflow.auth.domain.User;

import java.util.LinkedList;
import java.util.List;

public class UsersSystemRolesListResponse {
    private List<User> users;
    private List<Role> roles;

    public UsersSystemRolesListResponse() {
        users = new LinkedList<>();
        roles = new LinkedList<>();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}