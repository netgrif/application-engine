package com.netgrif.application.engine.petrinet.web.responsebodies;

import lombok.Data;

@Data
public class ProcessRole {

    private String stringId;

    private String name;

    private String description;

    public ProcessRole(String id, String name, String description) {
        this.stringId = id;
        this.name = name;
        this.description = description;
    }

    public ProcessRole() {
    }
}