package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.workflow.domain.Node;
import com.netgrif.application.engine.workflow.domain.Place;
import lombok.Data;

@Data
public class PlaceImportReference extends Node {

    private Integer tokens;
    private Boolean isStatic;

    public PlaceImportReference(Place place) {
        this.setTitle(place.getTitle());
        this.setObjectId(place.getObjectId());
        this.setImportId(place.getImportId());
        this.tokens = place.getTokens();
    }
}
