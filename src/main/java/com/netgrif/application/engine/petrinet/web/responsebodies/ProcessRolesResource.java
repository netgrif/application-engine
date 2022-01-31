package com.netgrif.application.engine.petrinet.web.responsebodies;


import com.netgrif.application.engine.petrinet.web.PetriNetController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class ProcessRolesResource extends EntityModel<ProcessRolesAndPermissions> {

    public ProcessRolesResource(ProcessRolesAndPermissions content, String netId) {
        super(content, new ArrayList<>());
        buildLinks(netId);
    }

    public ProcessRolesResource(Collection<com.netgrif.application.engine.petrinet.domain.roles.ProcessRole> content, Map<String, Map<String, Boolean>> permissions, String netId, Locale locale) {
        this(new ProcessRolesAndPermissions(content.stream().map(role -> new ProcessRole(
                role.getStringId(), role.getName().getTranslation(locale), role.getDescription()
        )).collect(Collectors.toList()), permissions), netId);
    }

    private void buildLinks(String netId) {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PetriNetController.class)
                .getRoles(netId, null)).withSelfRel());
    }
}