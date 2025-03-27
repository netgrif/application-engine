package com.netgrif.application.engine.authentication.web.responsebodies;


import com.netgrif.application.engine.authentication.web.UserController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

public class AuthoritiesResources extends CollectionModel<SessionRole> {
    public AuthoritiesResources(Iterable<SessionRole> content) {
        super(content);
        buildLinks();
    }

    public AuthoritiesResources() {
    }

    public void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .getAllAuthorities(null)).withSelfRel());
    }
}
