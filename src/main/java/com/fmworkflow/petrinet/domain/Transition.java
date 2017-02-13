package com.fmworkflow.petrinet.domain;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Transition extends Node {

    private int priority;

    public Transition() {
        super();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return this.getTitle();
    }
}
