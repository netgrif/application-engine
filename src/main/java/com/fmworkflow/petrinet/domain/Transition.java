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
    @Field("roles")
    // TODO: 18/02/2017 change Object to desired class
    private Map<String, Function<Object, Object>> roles;

    private int priority;

    public Transition() {
        super();
        dataSet = new HashMap<>();
        roles = new HashMap<>();
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

    public Map<String, Function<Object, Object>> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, Function<Object, Object>> roles) {
        this.roles = roles;
    }

    public void addRole(String fieldId, Function<Object, Object> role) {
        if (roles.containsKey(fieldId) && roles.get(fieldId) != null) {
            roles.put(fieldId, roles.get(fieldId).compose(role));
        } else {
            roles.put(fieldId, role);
        }
    }

    @Override
    public String toString() {
        return this.getTitle();
    }
}
