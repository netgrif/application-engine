package com.netgrif.application.engine.auth.web.responsebodies;


import com.netgrif.application.engine.auth.web.UserController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;

public class UserResource extends EntityModel<User> {

    public UserResource(User content, String selfRel) {
        super(content, new ArrayList<>());
        buildLinks(selfRel);
    }

    private void buildLinks(String selfRel) {
        WebMvcLinkBuilder getLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .getUser(getContent().getId(), false, null));
        add(selfRel.equalsIgnoreCase("profile") ? getLink.withSelfRel() : getLink.withRel("profile"));

        WebMvcLinkBuilder roleLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .assignRolesToUser(getContent().getId(), null, null));
        add(selfRel.equalsIgnoreCase("assignProcessRole") ? roleLink.withSelfRel() : roleLink.withRel("assignProcessRole"));

        WebMvcLinkBuilder authorityLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .assignAuthorityToUser(getContent().getId(), null));
        add(selfRel.equalsIgnoreCase("assignRole") ? authorityLink.withSelfRel() : authorityLink.withRel("assignRole"));
    }
}
