package com.netgrif.workflow.petrinet.web.responsebodies;


import lombok.Data;

@Data
public class PetriNetReference extends Reference {

    private String identifier;
    private String version;
    private String initials;

    public PetriNetReference() {
        super();
    }

    public PetriNetReference(String stringId, String identifier, String version, String title, String initials) {
        super(stringId, title);
        this.identifier = identifier;
        this.version = version;
        this.initials = initials;
    }
}