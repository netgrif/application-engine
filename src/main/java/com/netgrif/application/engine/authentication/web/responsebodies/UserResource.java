package com.netgrif.application.engine.authentication.web.responsebodies;


import com.netgrif.application.engine.authentication.web.UserController;
import com.netgrif.application.engine.authorization.web.RBACController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;

// TODO: release/8.0.0 remove User, replace with IUser
public class UserResource extends EntityModel<User> {

    public UserResource(User content, String selfRel) {
        super(content, new ArrayList<>());
        buildLinks(selfRel);
    }

    private void buildLinks(String selfRel) {
        WebMvcLinkBuilder getLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .getUser(getContent().getId(), null));
        add(selfRel.equalsIgnoreCase("profile") ? getLink.withSelfRel() : getLink.withRel("profile"));

        WebMvcLinkBuilder roleLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(RBACController.class)
                .assignRolesToUser(getContent().getId(), null));
        add(selfRel.equalsIgnoreCase("assignProcessRole") ? roleLink.withSelfRel() : roleLink.withRel("assignProcessRole"));

        WebMvcLinkBuilder authorityLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .assignAuthorityToUser(getContent().getId(), null));
        add(selfRel.equalsIgnoreCase("assignRole") ? authorityLink.withSelfRel() : authorityLink.withRel("assignRole"));
    }
}
