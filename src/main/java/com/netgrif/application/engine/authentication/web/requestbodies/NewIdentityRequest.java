package com.netgrif.application.engine.authentication.web.requestbodies;

import lombok.Data;

import java.util.Set;

@Data
public class NewIdentityRequest {

    public String email;
    public Set<String> groups;
    public Set<String> roles;

    public NewIdentityRequest() {
    }
}
