package com.netgrif.workflow.petrinet.web.responsebodies;

import com.netgrif.workflow.petrinet.web.PetriNetController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

public class PetriNetReferenceResourceAssembler implements ResourceAssembler<PetriNetReference, PetriNetReferenceResource> {
    @Override
    public PetriNetReferenceResource toResource(PetriNetReference petriNetReference) {
        return new PetriNetReferenceResource(petriNetReference);
    }

    public static void buildLinks(Resources resources) {
        resources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(PetriNetController.class).getAll(null, null, null, null))
                .withSelfRel());

        resources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(PetriNetController.class).getTransitionReferences(null, null, null))
                .withRel("transitions"));

        resources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(PetriNetController.class).getDataFieldReferences(null, null))
                .withRel("data"));

        resources.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                .methodOn(PetriNetController.class).searchPetriNets(null, null, null, null, null))
                .withRel("search"));
    }
}
