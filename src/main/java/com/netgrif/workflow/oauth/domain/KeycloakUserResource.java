package com.netgrif.workflow.oauth.domain;

import lombok.Getter;
import org.keycloak.representations.idm.UserRepresentation;

public class KeycloakUserResource implements RemoteUserResource {

    @Getter
    private final UserRepresentation representation;

    public KeycloakUserResource(UserRepresentation representation) {
        this.representation = representation;
    }

    @Override
    public String getUsername() {
        return representation.getUsername();
    }

    @Override
    public String getId() {
        return representation.getId();
    }

    @Override
    public String getEmail() {
        return representation.getEmail();
    }

    @Override
    public String getLastName() {
        return representation.getLastName();
    }

    @Override
    public String getFirstName() {
        return representation.getFirstName();
    }
}
