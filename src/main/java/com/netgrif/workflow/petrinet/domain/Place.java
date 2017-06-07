package com.netgrif.workflow.petrinet.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Place extends Node {

    @Getter @Setter
    private int tokens;

    @Getter @Setter
    private boolean isStatic;

    public Place() {
        super();
    }

    public void addTokens(int tokens) {
        this.tokens += tokens;
    }

    public void removeTokens(int tokens) throws IllegalArgumentException {
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
}
