package com.netgrif.application.engine.petrinet.domain;

import java.io.Serializable;

public abstract class Imported implements Serializable {

    private static final long serialVersionUID = -2708949961379974800L;

    protected String importId;

    public String getImportId() {
        return importId;
    }

    public void setImportId(String id) {
        this.importId = id;
    }
}