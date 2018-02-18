package com.netgrif.workflow.petrinet.web.responsebodies;


import lombok.Getter;
import lombok.Setter;

public class PetriNetReference {

    @Getter
    @Setter
    protected String entityId;
    @Getter
    @Setter
    protected String title;

    public PetriNetReference() {
    }

    public PetriNetReference(String entityId, String title) {
        this.entityId = entityId;
        this.title = title;
    }
}