package com.netgrif.workflow.petrinet.domain;

import lombok.Getter;
import lombok.Setter;

public class ResetArc extends Arc {

    @Getter @Setter
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
