package com.netgrif.workflow.petrinet.web.responsebodies;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

public class PetriNetReferenceResource extends Resource<PetriNetReference> {

    public PetriNetReferenceResource(PetriNetReference content) {
        super(content);
        buildLinks();
    }

    private void buildLinks(){

    }
}
