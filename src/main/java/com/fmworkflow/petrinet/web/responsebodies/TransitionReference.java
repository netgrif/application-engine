package com.fmworkflow.petrinet.web.responsebodies;


public class TransitionReference {

    private String entityId;
    private String petriNetId;
    private String title;

    public TransitionReference(){}

    public TransitionReference(String entityId, String petriNetId, String title) {
        this.entityId = entityId;
        this.petriNetId = petriNetId;
        this.title = title;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getPetriNetId() {
        return petriNetId;
    }

    public void setPetriNetId(String petriNetId) {
        this.petriNetId = petriNetId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
