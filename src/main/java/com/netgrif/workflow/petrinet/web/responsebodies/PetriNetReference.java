package com.netgrif.workflow.petrinet.web.responsebodies;


import lombok.Getter;
import lombok.Setter;

public class PetriNetReference {

    @Getter
    @Setter
    protected String entityId;

    @Getter
    @Setter
    protected String identifier;

    @Getter
    @Setter
    protected String version;

    @Getter
    @Setter
    protected String title;

    @Getter
    @Setter
    protected String initials;

    public PetriNetReference() {
    }

    public PetriNetReference(String entityId, String title){
        this.title = title;
        this.entityId = entityId;
    }

    public PetriNetReference(String entityId, String identifier, String version, String title, String initials) {
        this.entityId = entityId;
        this.identifier = identifier;
        this.version = version;
        this.title = title;
        this.initials = initials;
    }
}