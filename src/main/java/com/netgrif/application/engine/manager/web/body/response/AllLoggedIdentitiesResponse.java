package com.netgrif.application.engine.manager.web.body.response;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.manager.web.SessionManagerController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.Collection;


public class AllLoggedIdentitiesResponse extends CollectionModel<LoggedIdentity> {

    public AllLoggedIdentitiesResponse(Collection<LoggedIdentity> content) {
        super(content);
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SessionManagerController.class)
                .getAllSessions()).withSelfRel());
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SessionManagerController.class)
                .getAllSessions()).withRel("logoutAll"));
    }

}
