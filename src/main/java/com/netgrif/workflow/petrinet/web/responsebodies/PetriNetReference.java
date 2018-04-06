package com.netgrif.workflow.petrinet.web.responsebodies;


import lombok.Data;

@Data
public class PetriNetReference extends Reference {

    private String identifier;
    private String version;
    private String initials;
    private String defaultCaseName;

    public PetriNetReference() {
        super();
    }

    public PetriNetReference(String stringId, String identifier, String version, String title, String initials, String defaultCaseName) {
        super(stringId, title);
        this.identifier = identifier;
        this.version = version;
        this.initials = initials;
        this.defaultCaseName = defaultCaseName;
    }
}