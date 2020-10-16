package com.netgrif.workflow.petrinet.domain;

public enum DataEventPhase {
    PRE("pre"),
    POST("post");

    private final String value;

    DataEventPhase(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
