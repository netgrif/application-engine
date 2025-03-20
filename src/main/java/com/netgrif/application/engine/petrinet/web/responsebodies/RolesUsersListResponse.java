package com.netgrif.application.engine.petrinet.web.responsebodies;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
public class RolesUsersListResponse {
    List<IUser> users;

    public RolesUsersListResponse() {
        users = new LinkedList<>();
    }

}
