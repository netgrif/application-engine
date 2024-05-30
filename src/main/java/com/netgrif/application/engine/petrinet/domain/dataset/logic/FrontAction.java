package com.netgrif.application.engine.petrinet.domain.dataset.logic;

import lombok.Data;

import java.io.Serializable;

@Data
public class FrontAction implements Serializable {

    private static final long serialVersionUID = 3815234444390109824L;

    private String id;

    private Object args;

    public FrontAction(String id, Object args) {
        this.id = id;
        this.args = args;
    }
}
