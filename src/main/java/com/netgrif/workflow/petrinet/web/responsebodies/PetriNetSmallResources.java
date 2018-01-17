package com.netgrif.workflow.petrinet.web.responsebodies;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;

import java.util.ArrayList;

public class PetriNetSmallResources extends Resources<PetriNetSmallResource> {
    public PetriNetSmallResources(Iterable<PetriNetSmallResource> content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks(){

    }
}
