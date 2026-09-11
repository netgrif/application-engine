package com.netgrif.application.engine.objects.dto.response.petrinet;

import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;

import java.io.Serializable;
import java.util.Locale;

public record ProcessRoleDto(String stringId, String name, String description, String importId, String netImportId,
                             String netVersion, String netStringId, boolean global) implements Serializable {

    /**
     * This constructor doesn't set attributes regarding the Petri net.
     * <p>
     * Use the ProcessRoleFactory to create instances that have these attributes set.
     */
    public ProcessRoleDto(ProcessRole role, Locale locale) {
        this(role.getStringId(), role.getLocalisedName(locale), role.getDescription(), role.getImportId(), null, null, null, role.isGlobal());
    }

    public ProcessRoleDto(ProcessRole role, Locale locale, String netImportId,
                          String netVersion, String netStringId) {
        this(role.getStringId(), role.getLocalisedName(locale), role.getDescription(), role.getImportId(),netImportId, netVersion, netStringId, role.isGlobal());
    }

    public ProcessRoleDto(String id, String name, String description, boolean global) {
        this(id, name, description, null, null, null, null, global);
    }
}
