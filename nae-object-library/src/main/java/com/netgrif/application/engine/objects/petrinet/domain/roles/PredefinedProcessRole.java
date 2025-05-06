package com.netgrif.application.engine.objects.petrinet.domain.roles;

import lombok.Getter;

@Getter
public enum PredefinedProcessRole {

    DEFAULT_ROLE("default", "Default system process role"),
    ANONYMOUS_ROLE("anonymous", "Anonymous system process role");

    private final String name;
    private final String description;

    PredefinedProcessRole(String s, String d) {
        name = s;
        description = d;
    }
}
