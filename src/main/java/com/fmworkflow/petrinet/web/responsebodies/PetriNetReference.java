package com.fmworkflow.petrinet.web.responsebodies;


public class PetriNetReference {

    protected String entityId;
    protected String title;

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
