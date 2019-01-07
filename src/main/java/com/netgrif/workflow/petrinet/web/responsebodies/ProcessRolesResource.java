package com.netgrif.workflow.petrinet.web.responsebodies;


import com.netgrif.workflow.petrinet.web.PetriNetController;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class ProcessRolesResource extends Resources<ProcessRoleResource> {

    public ProcessRolesResource(Iterable<ProcessRoleResource> content, String netId) {
        super(content, new ArrayList<>());
        buildLinks(netId);
    }

    public ProcessRolesResource(Collection<com.netgrif.workflow.petrinet.domain.roles.ProcessRole> content, String netId, Locale locale) {
        this(content.stream()
                .map(role -> new ProcessRoleResource(
                        new ProcessRole(role.getStringId(), role.getName().getTranslation(locale), role.getDescription()
                        )))
                .collect(Collectors.toList()), netId);
    }

    private void buildLinks(String netId) {
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .getRoles(netId, null)).withSelfRel());
    }
}