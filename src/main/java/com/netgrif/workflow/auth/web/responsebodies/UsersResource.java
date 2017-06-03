package com.netgrif.workflow.auth.web.responsebodies;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.web.UserController;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class UsersResource extends Resources<UserResource> {
    public UsersResource(Iterable<UserResource> content, String selfRel) {
        super(content, new ArrayList<>());
        buildLinks(selfRel);
    }

    public UsersResource(Collection<User> content, String selfRel, boolean small) {
        this(content.stream().map(user -> new UserResource(user, small?"small":"profile", small))
                .collect(Collectors.toList()),selfRel);
    }

    private void buildLinks(String selfRel) {
        ControllerLinkBuilder allLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(UserController.class).getAll(null));
        add(selfRel.equalsIgnoreCase("all") ? allLink.withSelfRel() : allLink.withRel("all"));

        ControllerLinkBuilder smallLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(UserController.class).getAllSmall(null));
        add(selfRel.equalsIgnoreCase("small") ? smallLink.withSelfRel() : smallLink.withRel("small"));
    }
}
