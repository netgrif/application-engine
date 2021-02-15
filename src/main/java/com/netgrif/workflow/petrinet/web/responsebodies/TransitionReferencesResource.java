package com.netgrif.workflow.petrinet.web.responsebodies;

import com.netgrif.workflow.petrinet.web.PetriNetController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;


public class TransitionReferencesResource extends CollectionModel<TransitionReference> {

    public TransitionReferencesResource(Iterable<TransitionReference> content) {
        super(content, new ArrayList<Link>());
        buildLinks();
    }

    private void buildLinks(){
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PetriNetController.class)
                .getTransitionReferences(new ArrayList<>(), null, null)).withSelfRel());
    }
}
