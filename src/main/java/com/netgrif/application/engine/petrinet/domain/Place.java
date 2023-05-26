package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.petrinet.domain.arcs.reference.Referencable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Place extends Node implements Referencable {

    @Getter
    @Setter
    private Integer tokens;

    @Getter
    @Setter
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

    public Place clone() {
        Place clone = new Place();
        clone.setTokens(this.tokens);
        clone.setIsStatic(this.isStatic);
        clone.setTitle(this.getTitle());
        clone.setPosition(this.getPosition().getX(), this.getPosition().getY());
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.getImportId());
        return clone;
    }

    @Override
    public int getMultiplicity() {
        return tokens;
    }
}