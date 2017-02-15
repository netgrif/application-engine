package com.fmworkflow.petrinet.domain;

import com.fmworkflow.petrinet.domain.dataset.logic.ILogicFunction;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.Map;

@Document
public class Transition extends Node {
    @Field("dataSet")
    private Map<String, ILogicFunction> dataSet;

    private int priority;

    public Transition() {
        super();
        dataSet = new HashMap<>();
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
