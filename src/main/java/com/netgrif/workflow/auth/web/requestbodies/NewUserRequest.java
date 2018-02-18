package com.netgrif.workflow.auth.web.requestbodies;

import java.util.Set;

public class NewUserRequest {

    public String email;
    public Set<Long> organizations;
    public Set<String> processRoles;

    public NewUserRequest() {}
}
