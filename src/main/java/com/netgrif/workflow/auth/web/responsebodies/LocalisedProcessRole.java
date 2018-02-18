package com.netgrif.workflow.auth.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import lombok.Data;

import java.util.Locale;

@Data
@JsonRootName("processRole")
public class LocalisedProcessRole {

    private String stringId;

    private String name;

    private String description;

    public LocalisedProcessRole(ProcessRole role, Locale locale) {
        stringId = role.getStringId();
        name = role.getLocalisedName(locale);
        description = role.getDescription();
    }
}