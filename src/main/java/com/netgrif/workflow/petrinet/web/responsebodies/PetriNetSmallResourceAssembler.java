package com.netgrif.workflow.petrinet.web.responsebodies;

import org.springframework.hateoas.ResourceAssembler;

public class PetriNetSmallResourceAssembler implements ResourceAssembler<PetriNetSmall, PetriNetSmallResource>{

    @Override
    public PetriNetSmallResource toResource(PetriNetSmall petriNetSmall) {
        return new PetriNetSmallResource(petriNetSmall);
    }
}
