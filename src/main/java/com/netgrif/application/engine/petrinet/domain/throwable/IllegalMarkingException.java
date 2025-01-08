package com.netgrif.application.engine.petrinet.domain.throwable;

import com.netgrif.application.engine.workflow.domain.Place;

public class IllegalMarkingException extends IllegalStateException {

    public IllegalMarkingException(Place place) {
        super("Place (" + place.getImportId() + ") can not have negative number of tokens.");
    }
}
