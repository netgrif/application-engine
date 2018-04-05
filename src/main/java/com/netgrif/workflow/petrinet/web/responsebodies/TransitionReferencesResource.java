package com.netgrif.workflow.petrinet.web.responsebodies;

import com.netgrif.workflow.petrinet.web.PetriNetController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;


public class TransitionReferencesResource extends Resources<TransitionReference> {

    public TransitionReferencesResource(Iterable<TransitionReference> content) {
        super(content, new ArrayList<Link>());
        buildLinks();
    }

    private void buildLinks(){
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .getTransitionReferences(new ArrayList<>(), null, null)).withSelfRel());
    }
}
