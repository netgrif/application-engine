package com.netgrif.application.engine.workflow.domain.throwable;

import java.util.List;

public class MissingProcessMetaDataException extends Exception {

    public MissingProcessMetaDataException(List<String> missingMetaData) {
        super("Following properties from the imported net are missing: " + String.join(", ", missingMetaData));
    }
}
