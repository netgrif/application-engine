package com.netgrif.workflow.petrinet.domain;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Place extends Node {
    private int tokens;
    private boolean isStatic;

    public Place() {
        super();
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
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

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    @Override
    public String toString() {
        return getTitle() + " (" + tokens + ")";
    }
}
