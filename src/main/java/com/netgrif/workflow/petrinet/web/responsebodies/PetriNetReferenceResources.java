package com.netgrif.workflow.petrinet.web.responsebodies;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PetriNetReferenceResources extends Resources<PetriNetReferenceResource> {

    public PetriNetReferenceResources(Iterable<PetriNetReferenceResource> content, Link... links) {
        super(content, links);
        buildLinks();
    }

    public PetriNetReferenceResources(List<PetriNetReference> content) {
        this(content.stream().map(PetriNetReferenceResource::new).collect(Collectors.toList()));
    }

    private void buildLinks(){
        PetriNetReferenceResourceAssembler.buildLinks(this);
    }
}
