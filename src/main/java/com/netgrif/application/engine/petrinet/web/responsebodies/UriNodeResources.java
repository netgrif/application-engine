package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.orgstructure.web.GroupController;
import com.netgrif.application.engine.orgstructure.web.responsebodies.Group;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.web.UriController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

public class UriNodeResources extends CollectionModel<UriNode> {

    public UriNodeResources(Iterable<UriNode> content) {
        super(content);
        buildLinks();
    }

    private void buildLinks(){
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UriController.class)
                .getRoots()).withSelfRel());
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UriController.class)
                .getByLevel(0)).withSelfRel());
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UriController.class)
                .getByParent(null)).withSelfRel());
    }
}
