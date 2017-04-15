package com.fmworkflow.petrinet.domain;

public class ResetArc extends Arc {
    private Integer removedTokens;

    @Override
    public boolean isExecutable() {
        return true;
    }

    @Override
    public void execute() {
        Place place = ((Place) source);
        removedTokens = place.getTokens();
        place.removeAllTokens();
    }

    @Override
    public void rollbackExecution() {
        ((Place) source).addTokens(removedTokens);
    }
}
