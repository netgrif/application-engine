package com.fmworkflow.petrinet.web.responsebodies;


import com.fmworkflow.petrinet.domain.roles.ProcessRole;
import com.fmworkflow.petrinet.web.PetriNetController;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class RolesResource extends Resources<RoleResource>{

    public RolesResource(Iterable<RoleResource> content, String netId) {
        super(content, new ArrayList<>());
        buildLinks(netId);
    }

    public RolesResource(Collection<ProcessRole> content, String netId){
        this(content.stream().map(RoleResource::new).collect(Collectors.toList()), netId);
    }

    private void buildLinks(String netId){
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .getRoles(netId)).withSelfRel());
    }
}
