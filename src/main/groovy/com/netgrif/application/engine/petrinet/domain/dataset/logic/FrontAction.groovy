package com.netgrif.application.engine.petrinet.domain.dataset.logic;

class FrontAction implements Serializable {

    private static final long serialVersionUID = 3815234444390109824L;

    private String id;

    private Object args;

    FrontAction(String id, Object args) {
        this.id = id;
        this.args = args;
    }

    String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    Object getArgs() {
        return args;
    }

    void setArgs(Object args) {
        this.args = args;
    }
}
