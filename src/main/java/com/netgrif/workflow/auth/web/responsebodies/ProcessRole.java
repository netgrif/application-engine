package com.netgrif.workflow.auth.web.responsebodies;

import lombok.Data;

import java.util.Locale;

@Data
public class ProcessRole {

    private String stringId;

    private String name;

    private String description;

    public ProcessRole(com.netgrif.workflow.petrinet.domain.roles.ProcessRole role, Locale locale) {
        stringId = role.getStringId();
        name = role.getLocalisedName(locale);
        description = role.getDescription();
    }
}