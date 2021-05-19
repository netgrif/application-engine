package com.netgrif.workflow.oauth.domain;

import lombok.Getter;
import org.keycloak.representations.idm.GroupRepresentation;

public class KeycloakGroupResource implements RemoteGroupResource {

    @Getter
    private final GroupRepresentation representation;

    public KeycloakGroupResource(GroupRepresentation representation) {
        this.representation = representation;
    }

    @Override
    public String getName() {
        return representation.getName();
    }

    @Override
    public String getId() {
        return representation.getId();
    }

}
