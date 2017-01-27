package com.fmworkflow.petrinet.domain;

public class InhibitorArc extends Arc {
    public InhibitorArc(Arc object) {
        super(object.getSource(), object.getDestination(), object.getMultiplicity());
    }
}
