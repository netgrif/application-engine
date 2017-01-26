package com.fmworkflow.petrinet.domain;

import com.fmworkflow.Persistable;

public class Arc implements Persistable {
    private Node source;
    private Node destination;
    private int multiplicity;

    @Override
    public void persist() {
        System.out.println("Persisting Arc [" + toString() + " ]");
    }

    public Node getSource() {
        return source;
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public Node getDestination() {
        return destination;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
    }

    public int getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(int multiplicity) {
        this.multiplicity = multiplicity;
    }

    @Override
    public String toString() {
        return source.getTitle() + " -(" + multiplicity + ")> " + destination.getTitle();
    }
}
