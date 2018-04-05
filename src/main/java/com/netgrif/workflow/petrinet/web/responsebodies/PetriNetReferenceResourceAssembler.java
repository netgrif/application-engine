package com.netgrif.workflow.petrinet.web.responsebodies;

import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.Resources;

public class PetriNetReferenceResourceAssembler implements ResourceAssembler<PetriNetReference, PetriNetReferenceResource> {
    @Override
    public PetriNetReferenceResource toResource(PetriNetReference petriNetReference) {
        return new PetriNetReferenceResource(petriNetReference);
    }

    public static void buildLinks(Resources resources) {
        
    }
}
