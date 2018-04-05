package com.netgrif.workflow.petrinet.web.requestbodies;

import java.io.File;


public class UploadedFileMeta {

    public String name;
    public String initials;
    public String identifier;
    public String releaseType; //must be equal to PetriNet.VersionType

    public UploadedFileMeta() {}

    public UploadedFileMeta(String name, String initials, String identifier, String releaseType) {
        this.name = name;
        this.initials = initials;
        this.identifier = identifier;
        this.releaseType = releaseType;
    }
}
