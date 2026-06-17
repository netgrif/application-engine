package com.netgrif.application.engine.auth.web.requestbodies;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;

}