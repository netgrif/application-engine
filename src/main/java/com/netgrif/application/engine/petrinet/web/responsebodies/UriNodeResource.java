package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.web.UriController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

public class UriNodeResource extends EntityModel<UriNode> {

    public UriNodeResource(UriNode content) {
        super(content);
        buildLinks();
    }

    private void buildLinks() {
        UriNode content = getContent();
        if (content != null) {
            add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                            .methodOn(UriController.class).getRoot())
                    .withSelfRel());

            add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                            .methodOn(UriController.class).getOne(content.getUriPath()))
                    .withSelfRel());
        }
    }
}
