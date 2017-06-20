package com.netgrif.workflow.petrinet.domain;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.petrinet.domain.dataset.logic.IDataFunction;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import com.netgrif.workflow.workflow.domain.triggers.Trigger;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

@Document
public class Transition extends Node {

    @Field("dataSet")
    @Getter @Setter
    private Map<String, Set<IDataFunction>> dataSet;

    @Field("roles")
    @Getter @Setter
    private Map<String, Set<RolePermission>> roles;

    @DBRef
    @Getter @Setter
    private List<Trigger> triggers;

    @Getter @Setter
    private int priority;

    public Transition() {
        super();
        dataSet = new HashMap<>();
        roles = new HashMap<>();
        triggers = new LinkedList<>();
    }

    public ObjectNode applyDataLogic(String id, ObjectNode json) {
        for (IDataFunction function : dataSet.get(id)) {
            json = function.apply(json);
        }
        return json;
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

    public void addRole(String roleId, Set<RolePermission> permissions) {
        if (roles.containsKey(roleId) && roles.get(roleId) != null) {
            roles.get(roleId).addAll(permissions);
        } else {
            roles.put(roleId, permissions);
        }
    }

    public void addTrigger(Trigger trigger) {
        this.triggers.add(trigger);
    }

    @Override
    public String toString() {
        return this.getTitle();
    }
}
