package com.netgrif.application.engine.auth.web.requestbodies;

import lombok.Data;

import java.util.Set;

@Data
public class NewUserRequest {

    public String email;
    public Set<String> groups;
    public Set<String> processRoles;

    public NewUserRequest() {
    }
}
