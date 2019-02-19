package com.netgrif.workflow.auth.web.requestbodies;

import lombok.Data;

import java.util.List;

@Data
public class UserSearchRequestBody {

    private String fulltext;

    private List<String> roles;
}