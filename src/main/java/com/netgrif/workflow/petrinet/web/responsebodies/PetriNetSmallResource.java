package com.netgrif.workflow.petrinet.web.responsebodies;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;

public class PetriNetSmallResource extends Resource<PetriNetSmall> {
    public PetriNetSmallResource(PetriNetSmall content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks(){

    }
}
