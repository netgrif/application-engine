package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class FrontAction implements Serializable {

    @Serial
    private static final long serialVersionUID = 3815234444390109824L;
    private String id;
    private Object args;

    public FrontAction(String id, Object args) {
        this.id = id;
        this.args = args;
    }
}
