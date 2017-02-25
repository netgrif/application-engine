package com.fmworkflow.petrinet.web.responsebodies;

import com.fmworkflow.petrinet.web.PetriNetController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;


public class PetriNetReferencesResource extends Resources<PetriNetReference> {
    public PetriNetReferencesResource(Iterable<PetriNetReference> content) {
        super(content, new ArrayList<Link>());
        buildLinks();
    }

    private void buildLinks(){
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .getAllReferences()).withSelfRel());
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .getAll()).withRel("all"));
    }
}
