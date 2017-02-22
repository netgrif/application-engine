package com.fmworkflow.petrinet.domain;


public class TransitionReference {

    private String id;
    private String petriNetId;
    private String title;

    public TransitionReference(){}

    public TransitionReference(String id, String petriNetId, String title) {
        this.id = id;
        this.petriNetId = petriNetId;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPetriNetId() {
        return petriNetId;
    }

    public void setPetriNetId(String petriNetId) {
        this.petriNetId = petriNetId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
