package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.petrinet.web.PetriNetController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

public class PetriNetReferenceResource extends EntityModel<PetriNetReference> {

    public PetriNetReferenceResource(PetriNetReference content) {
        super(content);
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(PetriNetController.class).getOne(getContent().getStringId(), null, null))
                .withSelfRel());

        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(PetriNetController.class).getOne(getContent().getIdentifier(), getContent().getVersion(), null, null))
                .withRel("identifier"));

        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(PetriNetController.class).getRoles(getContent().getStringId(), null))
                .withRel("roles"));

        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(PetriNetController.class).getTransactions(getContent().getStringId(), null))
                .withRel("transaction"));

        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(PetriNetController.class).getNetFile(getContent().getStringId(), getContent().getTitle(), null, null))
                .withRel("file"));
    }
}