package com.netgrif.workflow.petrinet.web.responsebodies;


import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.web.PetriNetController;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class LocalisedRolesResource extends Resources<LocalisedRoleResource> {

    public LocalisedRolesResource(Iterable<LocalisedRoleResource> content, String netId) {
        super(content, new ArrayList<>());
        buildLinks(netId);
    }

    public LocalisedRolesResource(Collection<ProcessRole> content, String netId, Locale locale) {
        this(content.stream()
                .map(role -> new LocalisedRoleResource(
                        new LocalisedProcessRole(role.getStringId(), role.getName().getTranslation(locale), role.getDescription()
                        )))
                .collect(Collectors.toList()), netId);
    }

    private void buildLinks(String netId) {
        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .getRoles(netId, null)).withSelfRel());
    }
}