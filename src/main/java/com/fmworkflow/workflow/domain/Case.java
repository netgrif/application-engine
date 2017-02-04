package com.fmworkflow.workflow.domain;

import com.fmworkflow.petrinet.domain.PetriNet;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.Map;

@Document
public class Case {
    @DBRef
    private PetriNet petriNet;
    @Field("activePlaces")
    private Map<String, Integer> activePlaces;
    private String title;
    @Field("dataset")
    private DataSet dataSet;

    public Case() {
        activePlaces = new HashMap<>();
    }

    public Case(String title) {
        this();
        this.title = title;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public PetriNet getPetriNet() {
        return petriNet;
    }

    public void setPetriNet(PetriNet petriNet) {
        this.petriNet = petriNet;
    }

    public Map<String, Integer> getActivePlaces() {
        return activePlaces;
    }

    public void setActivePlaces(Map<String, Integer> activePlaces) {
        this.activePlaces = activePlaces;
    }

    public void addActivePlace(String placeId, Integer tokens) {
        this.activePlaces.put(placeId, tokens);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}