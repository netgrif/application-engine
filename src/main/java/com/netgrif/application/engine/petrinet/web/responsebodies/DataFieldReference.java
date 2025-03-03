package com.netgrif.application.engine.petrinet.web.responsebodies;


import lombok.Data;

@Data
public class DataFieldReference extends Reference {

    private String petriNetId;
    private String transitionId;

    public DataFieldReference() {
        super();
    }

    public DataFieldReference(String id, String title, String petriNetId, String transitionId) {
        super(id, title);
        this.petriNetId = petriNetId;
        this.transitionId = transitionId;
    }

    public DataFieldReference(String id, String title, TransitionReference transition) {
        super(id, title);
        this.petriNetId = transition.getPetriNetId();
        this.transitionId = transition.getStringId();
    }
}
