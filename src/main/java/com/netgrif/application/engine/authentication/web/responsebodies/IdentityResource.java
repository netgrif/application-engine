package com.netgrif.application.engine.authentication.web.responsebodies;


import com.netgrif.application.engine.authorization.web.RBACController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;
import java.util.Objects;

public class IdentityResource extends EntityModel<IdentityDTO> {

    public IdentityResource(IdentityDTO content, String selfRel) {
        super(content, new ArrayList<>());
        buildLinks(selfRel);
    }

    private void buildLinks(String selfRel) {
        WebMvcLinkBuilder getLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .getUser(Objects.requireNonNull(getContent()).getId(), null));
        add(selfRel.equalsIgnoreCase("profile") ? getLink.withSelfRel() : getLink.withRel("profile"));

        WebMvcLinkBuilder roleLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(RBACController.class)
                .assignRolesToActor(getContent().getId(), null));
        add(selfRel.equalsIgnoreCase("assignRolesToActor") ? roleLink.withSelfRel() : roleLink.withRel("assignRolesToActor"));

        WebMvcLinkBuilder authorityLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .assignAuthorityToUser(getContent().getId(), null));
        add(selfRel.equalsIgnoreCase("assignRole") ? authorityLink.withSelfRel() : authorityLink.withRel("assignRole"));
    }
}
