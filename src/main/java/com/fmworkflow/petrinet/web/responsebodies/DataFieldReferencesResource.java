package com.fmworkflow.petrinet.web.responsebodies;


import com.fmworkflow.petrinet.web.PetriNetController;
import com.fmworkflow.petrinet.web.requestbodies.PetriNetReferenceBody;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;

public class DataFieldReferencesResource extends Resources<DataFieldReference>{
    public DataFieldReferencesResource(Iterable<DataFieldReference> content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks(){
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .getDataFieldReferences(new PetriNetReferenceBody())).withSelfRel());
    }
}
