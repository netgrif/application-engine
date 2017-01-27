package com.fmworkflow.petrinet.domain;

public class ResetArc extends Arc {
    public ResetArc(Arc object) {
        super(object.getSource(), object.getDestination(), object.getMultiplicity());
    }
}
