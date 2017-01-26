package com.fmworkflow.petrinet.domain;

import com.fmworkflow.Persistable;

public class Transition extends Node {
    String title;

    @Override
    public void persist() {
        System.out.println("Persisting Transition [ " + this.toString() + " ]");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
