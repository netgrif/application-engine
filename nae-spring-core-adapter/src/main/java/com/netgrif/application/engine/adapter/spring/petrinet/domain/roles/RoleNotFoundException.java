package com.netgrif.application.engine.adapter.spring.petrinet.domain.roles;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message) {
        super(message);
    }
}
