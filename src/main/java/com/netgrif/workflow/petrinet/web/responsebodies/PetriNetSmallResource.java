package com.netgrif.workflow.petrinet.web.responsebodies;

import com.netgrif.workflow.petrinet.web.PetriNetController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;

public class PetriNetSmallResource extends Resource<PetriNetSmall> {
    public PetriNetSmallResource(PetriNetSmall content) {
        super(content, new ArrayList<>());
        buildLinks();
    }

    private void buildLinks() {
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .getNetFile(getContent().getStringId(), getContent().getTitle(), null, null)).withRel("file"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .getRoles(getContent().getStringId(), null)).withRel("roles"));
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .getTransactions(getContent().getStringId(), null)).withRel("transactions"));
    }
}
