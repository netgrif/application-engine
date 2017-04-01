package com.fmworkflow.petrinet.web.responsebodies;


public class DataFieldReference extends TransitionReference{

    private String transitionId;

    public DataFieldReference(){}

    public DataFieldReference(String entityId, String title, String petriNetId, String transitionId) {
        super(entityId, title, petriNetId);
        this.transitionId = transitionId;
    }

    public String getTransitionId() {
        return transitionId;
    }

    public void setTransitionId(String transitionId) {
        this.transitionId = transitionId;
    }
}
