package com.netgrif.workflow.petrinet.web.responsebodies;


import com.netgrif.workflow.petrinet.web.PetriNetController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class ProcessRolesResource extends CollectionModel<ProcessRolesAndPermissions> {

    public ProcessRolesResource(ProcessRolesAndPermissions content, String netId) {
        super(Collections.singleton(content), new ArrayList<>());
        buildLinks(netId);
    }

    public ProcessRolesResource(Collection<com.netgrif.workflow.petrinet.domain.roles.ProcessRole> content, Map<String, Map<String, Boolean>> permissions, String netId, Locale locale) {
        this(new ProcessRolesAndPermissions(content.stream().map(role -> new ProcessRole(
                role.getStringId(), role.getName().getTranslation(locale), role.getDescription()
        )).collect(Collectors.toList()), permissions), netId);
    }

    private void buildLinks(String netId) {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PetriNetController.class)
                .getRoles(netId, null)).withSelfRel());
    }
}