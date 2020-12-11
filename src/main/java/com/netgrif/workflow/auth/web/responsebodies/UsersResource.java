package com.netgrif.workflow.auth.web.responsebodies;


import com.netgrif.workflow.auth.web.UserController;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class UsersResource extends Resources<UserResource> {

    public UsersResource(Collection<UserResource> content, String selfRel) {
        super(content, new ArrayList<>());
        buildLinks(selfRel);
    }

    public UsersResource(Collection<User> content, String selfRel, boolean small) {
        this(content.stream().map(user -> new UserResource(user, small ? "small" : "profile"))
                .collect(Collectors.toList()), selfRel);
    }

    private void buildLinks(String selfRel) {
        ControllerLinkBuilder allLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(UserController.class).getAll(false, null,null, null, null));
        add(selfRel.equalsIgnoreCase("all") ? allLink.withSelfRel() : allLink.withRel("all"));
    }
}