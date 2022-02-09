package com.netgrif.application.engine.petrinet.domain.throwable;

import java.util.List;

public class MissingPetriNetMetaDataException extends Exception {

    public MissingPetriNetMetaDataException(List<String> missingMetaData) {
        super("Following properties from the imported net are missing: " + String.join(", ", missingMetaData));
    }
}
