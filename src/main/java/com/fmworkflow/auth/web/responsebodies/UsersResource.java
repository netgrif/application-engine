package com.fmworkflow.auth.web.responsebodies;


import com.fmworkflow.auth.domain.User;
import org.springframework.hateoas.Resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class UsersResource extends Resources<UserResource> {
    public UsersResource(Iterable<UserResource> content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    public UsersResource(Collection<User> content, boolean small) {
        this(content.stream().map(user -> new UserResource(user,small)).collect(Collectors.toList()));
    }

    private void buildLinks() {

    }
}
