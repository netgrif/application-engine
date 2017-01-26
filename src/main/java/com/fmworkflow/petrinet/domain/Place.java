package com.fmworkflow.petrinet.domain;

public class Place extends Node {
    private int tokens;
    private boolean isStatic;

    @Override
    public void persist() {
        System.out.println("Persisting Place [ " + this.toString() + " ]");
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
