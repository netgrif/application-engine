package com.fmworkflow.auth.web.responsebodies;


import com.fmworkflow.auth.domain.Role;
import com.fmworkflow.auth.web.UserController;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;

public class AuthoritiesResources extends Resources<Role> {
    public AuthoritiesResources(Iterable<Role> content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    public AuthoritiesResources() {
    }

    public void buildLinks(){
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(UserController.class)
                .getAllAuthorities(null)).withSelfRel());
    }
}
