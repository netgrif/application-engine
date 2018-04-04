package com.netgrif.workflow.petrinet.web.responsebodies;


import lombok.Data;

@Data
public class TransitionReference extends PetriNetReference{

    protected String petriNetId;

    public TransitionReference(){}

    public TransitionReference(String id, String title, String petriNetId) {
        super(id, title);
        this.petriNetId = petriNetId;
    }

    public TransitionReference(String id, String title, PetriNetReference net) {
        super(id, title);
        this.petriNetId = net.getStringId();
    }
}
