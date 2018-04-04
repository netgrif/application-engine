package com.netgrif.workflow.petrinet.web.responsebodies;


import lombok.Data;

@Data
public class PetriNetReference {

    protected String stringId;

    private String identifier;

    private String version;

    protected String title;

    private String initials;

    public PetriNetReference() {
    }

    public PetriNetReference(String stringId, String title) {
        this.title = title;
        this.stringId = stringId;
    }

    public PetriNetReference(String stringId, String identifier, String version, String title, String initials) {
        this.stringId = stringId;
        this.identifier = identifier;
        this.version = version;
        this.title = title;
        this.initials = initials;
    }
}