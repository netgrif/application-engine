package com.netgrif.workflow.petrinet.web.responsebodies;

import com.netgrif.workflow.petrinet.web.PetriNetController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ControllerLinkBuilderFactory;

public class PetriNetReferenceResource extends Resource<PetriNetReference> {

    public PetriNetReferenceResource(PetriNetReference content) {
        super(content);
        buildLinks();
    }

    private void buildLinks() {
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(PetriNetController.class).getOne(getContent().getStringId(), null, null))
                .withSelfRel());

        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(PetriNetController.class).getOne(getContent().getIdentifier(), getContent().getVersion(), null, null))
                .withRel("identifier"));

        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(PetriNetController.class).getRoles(getContent().getStringId(), null))
                .withRel("roles"));

        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(PetriNetController.class).getTransactions(getContent().getStringId(), null))
                .withRel("transaction"));

        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(PetriNetController.class).getNetFile(getContent().getStringId(), getContent().getTitle(), null, null))
                .withRel("file"));
    }
}
