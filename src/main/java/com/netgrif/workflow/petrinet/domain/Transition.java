package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import com.netgrif.workflow.workflow.domain.triggers.Trigger;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;
import java.util.stream.Collectors;

@Document
public class Transition extends Node {

    @Field("dataGroups")
    @Getter @Setter
    private Map<String, DataGroup> dataGroups;

    @Field("dataSet")
    @Getter @Setter
    private LinkedHashMap<String, DataFieldLogic> dataSet;

    @Field("roles")
    @Getter @Setter
    private Map<String, Set<RolePermission>> roles;

    @Field("triggers")
    @Getter @Setter
    private List<Trigger> triggers;

    @Getter @Setter
    private Integer priority;

    @Getter @Setter
    private String icon;

    @Getter @Setter
    private DataFocusPolicy dataFocusPolicy;

    public Transition() {
        super();
        dataSet = new LinkedHashMap<>();
        roles = new HashMap<>();
        triggers = new LinkedList<>();
        dataGroups = new LinkedHashMap<>();
        dataFocusPolicy = DataFocusPolicy.MANUAL;
    }

    public void addDataSet(String fieldId, DataFieldLogic logic) {
        if (dataSet.containsKey(fieldId) && dataSet.get(fieldId) != null) {
            dataSet.get(fieldId).merge(logic);
        } else {
            dataSet.put(fieldId, logic);
        }
    }

    public void addDataSet(String field, Set<FieldBehavior> behavior, Set<Action> actions){
        if(dataSet.containsKey(field) && dataSet.get(field) != null){
            if(behavior != null) dataSet.get(field).getBehavior().addAll(behavior);
            if(actions != null) dataSet.get(field).getActions().addAll(actions);
        } else {
            dataSet.put(field,new DataFieldLogic(behavior, actions));
        }
    }

    public void addActions(String field, LinkedHashSet<Action> actions){
        if(dataSet.containsKey(field)){
            dataSet.get(field).setActions(actions);
        }
    }

    public void addRole(String roleId, Set<RolePermission> permissions) {
        if (roles.containsKey(roleId) && roles.get(roleId) != null) {
            roles.get(roleId).addAll(permissions);
        } else {
            roles.put(roleId, permissions);
        }
    }

    public void addDataGroup(DataGroup dataGroup) {
        dataGroups.put(dataGroup.getStringId(), dataGroup);
    }

    public void addTrigger(Trigger trigger) {
        this.triggers.add(trigger);
    }

    public boolean isDisplayable(String fieldId){
        DataFieldLogic logic = dataSet.get(fieldId);
        return logic != null && logic.isDisplayable();
    }

    public List<String> getImmediateData(){
        return dataSet.entrySet().stream().filter(entry -> entry.getValue().getBehavior().contains(FieldBehavior.IMMEDIATE))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return this.getTitle().toString();
    }
}