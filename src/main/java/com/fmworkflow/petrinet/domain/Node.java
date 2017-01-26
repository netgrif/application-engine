package com.fmworkflow.petrinet.domain;

import com.fmworkflow.Persistable;

import java.util.Set;

public abstract class Node extends Editable2D {
    private String title;
    private Set<Arc> arcs;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
