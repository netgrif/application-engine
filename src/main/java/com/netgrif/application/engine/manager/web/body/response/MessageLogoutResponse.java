package com.netgrif.application.engine.manager.web.body.response;

import com.netgrif.application.engine.manager.web.SessionManagerController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

public class MessageLogoutResponse extends EntityModel<Boolean> {

    public MessageLogoutResponse(boolean content) {
        super(content);
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SessionManagerController.class)
                .getAllSessions()).withRel("get all session"));
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SessionManagerController.class)
                .getAllSessions()).withSelfRel());
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SessionManagerController.class)
                .getAllSessions()).withRel("logout All"));
    }

}
