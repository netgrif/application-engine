package com.fmworkflow.petrinet.domain;

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
