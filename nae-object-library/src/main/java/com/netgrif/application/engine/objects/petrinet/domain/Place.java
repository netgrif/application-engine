package com.netgrif.application.engine.objects.petrinet.domain;

import com.netgrif.application.engine.objects.petrinet.domain.arcs.reference.Referencable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Place extends Node implements Referencable {

    private Integer tokens;

    private Boolean isStatic;

    public Place() {
        super();
    }

    public void addTokens(int tokens) {
        this.tokens += tokens;
    }

    public void removeTokens(Integer tokens) throws IllegalArgumentException {
        if (this.tokens - tokens < 0)
            throw new IllegalArgumentException("Place can not have negative number of tokens.");
        this.tokens -= tokens;
    }

    public void removeAllTokens() {
        this.tokens = 0;
    }

    @Override
    public String toString() {
        return getTitle() + " (" + tokens + ")";
    }

    public Place(Place place) {
        this.setTokens(place.tokens);
        this.setIsStatic(place.isStatic);
        this.setTitle(place.getTitle());
        this.setPosition(place.getPosition());
        this.setObjectId(place.getObjectId());
        this.setImportId(place.getImportId());
    }

    @Override
    public int getMultiplicity() {
        return tokens;
    }
}
