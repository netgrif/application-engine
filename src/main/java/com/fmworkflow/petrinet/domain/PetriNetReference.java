package com.fmworkflow.petrinet.domain;


public class PetriNetReference {

    private String entityId;
    private String title;

    public PetriNetReference(){}

    public PetriNetReference(String entityId, String title) {
        this.entityId = entityId;
        this.title = title;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
