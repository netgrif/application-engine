package com.fmworkflow.petrinet.domain;

import com.fmworkflow.petrinet.domain.dataset.logic.ILogicFunction;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Document
public class Transition extends Node {
    @Field("dataSet")
    private Map<String, Function<JSONObject, JSONObject>> dataSet;

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

    public Map<String, Function<JSONObject, JSONObject>> getDataSet() {
        return dataSet;
    }

    public void setDataSet(Map<String, Function<JSONObject, JSONObject>> dataSet) {
        this.dataSet = dataSet;
    }

    public void addDataSet(String fieldId, ILogicFunction function) {
        if (dataSet.containsKey(fieldId) && dataSet.get(fieldId) != null) {
            dataSet.put(fieldId, dataSet.get(fieldId).compose(function));
        } else {
            dataSet.put(fieldId, function);
        }
    }

    @Override
    public String toString() {
        return this.getTitle();
    }
}
