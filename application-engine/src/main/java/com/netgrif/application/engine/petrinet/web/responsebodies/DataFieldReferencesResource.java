package com.netgrif.application.engine.petrinet.web.responsebodies;


import com.netgrif.application.engine.objects.dto.response.petrinet.DataFieldReferenceDto;
import com.netgrif.application.engine.petrinet.web.PetriNetController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;

public class DataFieldReferencesResource extends CollectionModel<DataFieldReferenceDto> {
    public DataFieldReferencesResource(Iterable<DataFieldReferenceDto> content) {
        super(content);
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PetriNetController.class)
                .getDataFieldReferences(new ArrayList<>(), null)).withSelfRel());
    }
}
