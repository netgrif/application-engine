package com.fmworkflow.petrinet.domain;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fmworkflow.petrinet.domain.dataset.logic.IDataFunction;
import com.fmworkflow.petrinet.domain.roles.IRoleFunction;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Document
public class Transition extends Node {
    @Field("dataSet")
    private Map<String, Set<IDataFunction>> dataSet;
    @Field("roles")
    private Map<String, Set<IRoleFunction>> roles;

    private int priority;

    public Transition() {
        super();
        dataSet = new HashMap<>();
        roles = new HashMap<>();
    }

    public ObjectNode applyDataLogic(String id, ObjectNode json) {
        for (IDataFunction function : dataSet.get(id)) {
            json = function.apply(json);
        }
        return json;
    }

    public ObjectNode applyRoleLogic(String id, ObjectNode json) {
        for (IRoleFunction function : roles.get(id)) {
            json = function.apply(json);
        }
        return json;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Map<String, Set<IDataFunction>> getDataSet() {
        return dataSet;
    }

    public void setDataSet(Map<String, Set<IDataFunction>> dataSet) {
        this.dataSet = dataSet;
    }

    public void addDataSet(String fieldId, IDataFunction function) {
        if (dataSet.containsKey(fieldId) && dataSet.get(fieldId) != null) {
            dataSet.get(fieldId).add(function);
        } else {
            Set<IDataFunction> logic = new HashSet<>();
            logic.add(function);
            dataSet.put(fieldId, logic);
        }
    }

    public Map<String, Set<IRoleFunction>> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, Set<IRoleFunction>> roles) {
        this.roles = roles;
    }

    public void addRole(String fieldId, IRoleFunction function) {
        if (roles.containsKey(fieldId) && roles.get(fieldId) != null) {
            roles.get(fieldId).add(function);
        } else {
            Set<IRoleFunction> logic = new HashSet<>();
            logic.add(function);
            roles.put(fieldId, logic);
        }
    }

    @Override
    public String toString() {
        return this.getTitle();
    }
}
