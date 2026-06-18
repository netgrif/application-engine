package com.netgrif.application.engine.petrinet.domain.throwable;

import java.util.List;

public class MissingPetriNetMetaDataException extends Exception {

    public MissingPetriNetMetaDataException(List<String> missingMetaData, String processId) {
        super("Following properties from the imported net '" + processId + "' are missing: " + String.join(", ", missingMetaData));
    }
}
