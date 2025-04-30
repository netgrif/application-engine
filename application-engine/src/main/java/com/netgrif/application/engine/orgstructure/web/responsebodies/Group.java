package com.netgrif.application.engine.orgstructure.web.responsebodies;

import lombok.Data;

@Data
public class Group {

    private String id;
    private String name;

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
