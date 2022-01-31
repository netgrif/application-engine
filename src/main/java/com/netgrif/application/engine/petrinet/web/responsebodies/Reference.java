package com.netgrif.application.engine.petrinet.web.responsebodies;

import lombok.Data;

@Data
public class Reference {

    protected String stringId;
    protected String title;

    public Reference() {
    }

    public Reference(String stringId, String title) {
        this.stringId = stringId;
        this.title = title;
    }
}
