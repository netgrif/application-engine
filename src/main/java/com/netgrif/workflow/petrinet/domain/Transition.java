package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import com.netgrif.workflow.workflow.domain.Trigger;
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
    private LinkedHashMap<String, DataFieldLogic> dataSet;

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
        dataSet = new LinkedHashMap<>();
        roles = new HashMap<>();
        triggers = new LinkedList<>();
    }

//    public ObjectNode applyDataLogic(String id, ObjectNode json) {
//        for (IDataFunction function : dataSet.get(id)) {
//            json = function.apply(json);
//        }
//        return json;
//    }

//    public ObjectNode applyRoleLogic(String id, ObjectNode json) {
//        for (IRoleFunction function : roles.get(id)) {
//            json = function.apply(json);
//        }
//        return json;
//    }

    public void addDataSet(String fieldId, DataFieldLogic logic) {
        if (dataSet.containsKey(fieldId) && dataSet.get(fieldId) != null) {
            dataSet.get(fieldId).merge(logic);
        } else {
            dataSet.put(fieldId, logic);
        }
    }

    public void addDataSet(String field, Set<FieldBehavior> behavior, Set<String> actions){
        if(dataSet.containsKey(field) && dataSet.get(field) != null){
            if(behavior != null) dataSet.get(field).getBehavior().addAll(behavior);
            if(behavior != null) dataSet.get(field).getActions().addAll(actions);
        } else {
            dataSet.put(field,new DataFieldLogic(behavior, actions));
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

    public boolean isDisplayable(String fieldId){
        return dataSet.get(fieldId).isDisplayable();
    }

    @Override
    public String toString() {
        return this.getTitle();
    }
}
