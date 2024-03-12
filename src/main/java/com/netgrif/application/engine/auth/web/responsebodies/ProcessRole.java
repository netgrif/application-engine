package com.netgrif.application.engine.auth.web.responsebodies;

import lombok.Data;

import java.util.Locale;

@Data
public class ProcessRole {

    private String stringId;

    private String name;

    private String description;

    private String importId;

    // net attributes are set in the Factory service
    private String netImportId;

    private String netVersion;

    private String netStringId;

    /**
     * The constructor doesn't set attributes regarding the Petri net.
     *
     * Use the ProcessRoleFactory to create instances that have these attributes set.
     */
    public ProcessRole(com.netgrif.application.engine.petrinet.domain.roles.ProcessRole role, Locale locale) {
        stringId = role.getStringId();
        name = role.getLocalisedName(locale);
        description = role.getDescription();
        importId = role.getImportId();
    }
}