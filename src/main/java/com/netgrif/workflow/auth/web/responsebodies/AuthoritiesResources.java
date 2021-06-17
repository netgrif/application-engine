package com.netgrif.workflow.auth.web.responsebodies;


import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.web.UserController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;

public class AuthoritiesResources extends CollectionModel<Authority> {
    public AuthoritiesResources(Iterable<Authority> content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    public AuthoritiesResources() {
    }

    public void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .getAllAuthorities(null)).withSelfRel());
    }
}
