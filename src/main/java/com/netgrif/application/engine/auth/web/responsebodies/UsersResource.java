package com.netgrif.application.engine.auth.web.responsebodies;


import com.netgrif.application.engine.auth.web.UserController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class UsersResource extends CollectionModel<UserResource> {

    public static final String SELF_REL_SMALL = "small";
    public static final String SELF_REL_PROFILE = "profile";

    public UsersResource(Collection<UserResource> content, String selfRel) {
        super(content, new ArrayList<>(), null);
        buildLinks(selfRel);
    }

    public UsersResource(Collection<User> content, String selfRel, boolean small) {
        this(content.stream().map(user -> new UserResource(user, small ? SELF_REL_SMALL : SELF_REL_PROFILE))
                .collect(Collectors.toList()), selfRel);
    }

    private void buildLinks(String selfRel) {
        WebMvcLinkBuilder allLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(UserController.class).getAll(false, null, null, null, null));
        add(selfRel.equalsIgnoreCase("all") ? allLink.withSelfRel() : allLink.withRel("all"));
    }
}