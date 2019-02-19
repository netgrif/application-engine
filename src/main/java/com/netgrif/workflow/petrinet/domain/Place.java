package com.netgrif.workflow.petrinet.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Place extends Node {

    @Getter @Setter
    private Integer tokens;

    @Getter @Setter
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
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.getImportId());
        return clone;
    }
}