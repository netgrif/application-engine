package com.fmworkflow.petrinet.domain;

public class InhibitorArc extends Arc {
    @Override
    public boolean isExecutable() {
        if (source instanceof Transition)
            return true;
        return ((Place) source).getTokens() < multiplicity;
    }
}