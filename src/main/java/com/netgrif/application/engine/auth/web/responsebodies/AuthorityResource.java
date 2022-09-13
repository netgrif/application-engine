package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.web.AuthorityController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;

public class AuthorityResource extends EntityModel<Authority> {

    public AuthorityResource(Authority content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(AuthorityController.class).getAll()).withRel("getAll"));
    }
}
