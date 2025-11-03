package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.objects.dto.response.petrinet.PetriNetReferenceDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;

import java.util.List;
import java.util.stream.Collectors;

public class PetriNetReferenceResources extends CollectionModel<PetriNetReferenceResource> {

    public PetriNetReferenceResources(Iterable<PetriNetReferenceResource> content, Iterable<Link> links) {
        super(content, links, null);
        buildLinks();
    }

    public PetriNetReferenceResources(Iterable<PetriNetReferenceResource> content) {
        super(content);
        buildLinks();
    }

    public PetriNetReferenceResources(List<PetriNetReferenceDto> content) {
        this(content.stream().map(PetriNetReferenceResource::new).collect(Collectors.toList()));
    }

    private void buildLinks() {
        PetriNetReferenceResourceAssembler.buildLinks(this);
    }
}
