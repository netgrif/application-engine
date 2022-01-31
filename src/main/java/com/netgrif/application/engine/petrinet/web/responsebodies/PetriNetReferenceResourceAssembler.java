package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.petrinet.web.PetriNetController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

public class PetriNetReferenceResourceAssembler implements RepresentationModelAssembler<PetriNetReference, PetriNetReferenceResource> {
    @Override
    public PetriNetReferenceResource toModel(PetriNetReference petriNetReference) {
        return new PetriNetReferenceResource(petriNetReference);
    }

    public static void buildLinks(CollectionModel resources) {
        resources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(PetriNetController.class).getAll(null, null, null, null))
                .withSelfRel());

        resources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(PetriNetController.class).getTransitionReferences(null, null, null))
                .withRel("transitions"));

        resources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(PetriNetController.class).getDataFieldReferences(null, null))
                .withRel("data"));

        resources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                .methodOn(PetriNetController.class).searchPetriNets(null, null, null, null, null))
                .withRel("search"));
    }
}
