package com.netgrif.application.engine.manager.web.body.response;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.manager.web.SessionManagerController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.Collection;


public class AllLoggedUsersResponse extends CollectionModel<LoggedUser> {

    public AllLoggedUsersResponse(Collection<LoggedUser> content) {
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
