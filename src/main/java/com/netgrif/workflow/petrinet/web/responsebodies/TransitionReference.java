package com.netgrif.workflow.petrinet.web.responsebodies;


public class TransitionReference extends PetriNetReference{

    protected String petriNetId;

    public TransitionReference(){}

    public TransitionReference(String entityId, String title, String petriNetId) {
        super(entityId, title);
        this.petriNetId = petriNetId;
    }

    public String getPetriNetId() {
        return petriNetId;
    }

    public void setPetriNetId(String petriNetId) {
        this.petriNetId = petriNetId;
    }
}
