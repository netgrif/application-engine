package com.fmworkflow.petrinet.domain;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fmworkflow.petrinet.domain.dataset.logic.LogicFunction;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Document
public class Transition extends Node {
    @Field("dataSet")
    private Map<String, Function<ObjectNode, ObjectNode>> dataSet;
    @Field("roles")
    private Map<String, Function<ObjectNode, ObjectNode>> roles;

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

    public Map<String, Function<ObjectNode, ObjectNode>> getDataSet() {
        return dataSet;
    }

    public void setDataSet(Map<String, Function<ObjectNode, ObjectNode>> dataSet) {
        this.dataSet = dataSet;
    }

    public void addDataSet(String fieldId, LogicFunction function) {
        if (dataSet.containsKey(fieldId) && dataSet.get(fieldId) != null) {
            dataSet.put(fieldId, dataSet.get(fieldId).compose(function));
        } else {
            dataSet.put(fieldId, function);
        }
    }

    public Map<String, Function<ObjectNode, ObjectNode>> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, Function<ObjectNode, ObjectNode>> roles) {
        this.roles = roles;
    }

    public void addRole(String fieldId, Function<ObjectNode, ObjectNode> role) {
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
