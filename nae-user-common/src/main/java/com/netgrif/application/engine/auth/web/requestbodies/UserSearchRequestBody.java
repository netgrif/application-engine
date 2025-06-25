package com.netgrif.application.engine.auth.web.requestbodies;

import lombok.Data;

import java.util.List;

@Data
public class UserSearchRequestBody {

    private String realmId;

    private String fulltext;

    private List<String> roles;

    private List<String> negativeRoles;
}