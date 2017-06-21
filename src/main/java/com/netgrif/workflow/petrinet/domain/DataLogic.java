package com.netgrif.workflow.petrinet.domain;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.petrinet.domain.dataset.logic.DataBehavior;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


public class DataLogic {

    @Getter
    @Setter
    private Set<DataBehavior> behavior;

    @Getter
    @Setter
    private Set<String> actions;

    public DataLogic() {
        this.behavior = new HashSet<>();
        this.actions = new HashSet<>();
    }

    public DataLogic(Set<DataBehavior> behavior, Set<String> actions) {
        this();
        if(behavior != null) this.behavior.addAll(behavior);
        if(actions != null) this.actions.addAll(actions);
    }

    public ObjectNode applyBehavior(ObjectNode jsonNode){
        behavior.forEach(dataBehavior -> jsonNode.put(dataBehavior.toString(),true));
        return jsonNode;
    }

    public ObjectNode applyBehavior(){
        return applyBehavior(JsonNodeFactory.instance.objectNode());
    }

    public void merge(DataLogic other){
        this.behavior.addAll(other.behavior);
        this.actions.addAll(other.actions);
    }

    public void makeVisible(){
        this.behavior.remove(DataBehavior.EDITABLE);
        this.behavior.add(DataBehavior.VISIBLE);
    }

    public void makeEditable(){
        this.behavior.remove(DataBehavior.VISIBLE);
        this.behavior.add(DataBehavior.EDITABLE);
    }

    public void makeRequired(){
        this.behavior.remove(DataBehavior.OPTIONAL);
        this.behavior.add(DataBehavior.REQUIRED);
    }

    public void makeOptional(){
        this.behavior.remove(DataBehavior.REQUIRED);
        this.behavior.add(DataBehavior.OPTIONAL);
    }
}
