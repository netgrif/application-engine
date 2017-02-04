package com.fmworkflow.petrinet.domain;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Transition extends Node {

    public Transition() {
        super();
    }

    @Override
    public String toString() {
        return this.getTitle();
    }
}
