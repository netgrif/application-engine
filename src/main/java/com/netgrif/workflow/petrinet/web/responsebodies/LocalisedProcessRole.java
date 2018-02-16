package com.netgrif.workflow.petrinet.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

@Data
@JsonRootName("processRole")
public class LocalisedProcessRole {

    private String stringId;

    private String name;

    private String description;

    public LocalisedProcessRole(String id, String name, String description) {
        this.stringId = id;
        this.name = name;
        this.description = description;
    }

    public LocalisedProcessRole() {
    }
}