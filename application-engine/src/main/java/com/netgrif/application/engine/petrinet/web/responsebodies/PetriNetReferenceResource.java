package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.objects.dto.response.petrinet.PetriNetReferenceDto;
import com.netgrif.application.engine.petrinet.web.PetriNetController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

public class PetriNetReferenceResource extends EntityModel<PetriNetReferenceDto> {

    public PetriNetReferenceResource(PetriNetReferenceDto content) {
        super(content);
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                        .methodOn(PetriNetController.class).getOne(getContent().stringId(), null, null))
                .withSelfRel());

        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                        .methodOn(PetriNetController.class).getOne(getContent().identifier(), getContent().version(), null, null))
                .withRel("identifier"));

        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                        .methodOn(PetriNetController.class).getRoles(getContent().stringId(), null))
                .withRel("roles"));

        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                        .methodOn(PetriNetController.class).getTransactions(getContent().stringId(), null))
                .withRel("transaction"));

        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                        .methodOn(PetriNetController.class).getNetFile(getContent().stringId(), getContent().title(), null, null))
                .withRel("file"));
    }
}