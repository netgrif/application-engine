package com.netgrif.workflow.petrinet.web.responsebodies;


import lombok.Data;

@Data
public class DataFieldReference extends TransitionReference {

    private String transitionId;

    public DataFieldReference() {
    }

    public DataFieldReference(String id, String title, String petriNetId, String transitionId) {
        super(id, title, petriNetId);
        this.transitionId = transitionId;
    }

    public DataFieldReference(String id, String title, TransitionReference transition) {
        super(id, title, transition.getPetriNetId());
        this.transitionId = transition.getStringId();
    }
}
