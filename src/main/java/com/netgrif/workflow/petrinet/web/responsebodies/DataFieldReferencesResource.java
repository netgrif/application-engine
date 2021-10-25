package com.netgrif.workflow.petrinet.web.responsebodies;


import com.netgrif.workflow.petrinet.web.PetriNetController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;

public class DataFieldReferencesResource extends CollectionModel<DataFieldReference> {
    public DataFieldReferencesResource(Iterable<DataFieldReference> content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PetriNetController.class)
                .getDataFieldReferences(new ArrayList<>(), null)).withSelfRel());
    }
}
