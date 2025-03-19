package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.core.petrinet.domain.Node;
import com.netgrif.core.petrinet.domain.Place;
import lombok.Data;

@Data
public class PlaceImportReference extends Node {

    private Integer tokens;

    private Boolean isStatic;

    public PlaceImportReference(Place place) {
        this.setPosition(place.getPosition());
        this.setTitle(place.getTitle());
        this.setObjectId(place.getObjectId());
        this.setImportId(place.getImportId());
        this.tokens = place.getTokens();
        this.isStatic = place.getIsStatic();
    }
}
