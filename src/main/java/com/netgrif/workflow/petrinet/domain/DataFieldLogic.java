package com.netgrif.workflow.petrinet.domain;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


public class DataFieldLogic {

    @Getter
    @Setter
    private Set<FieldBehavior> behavior;

    @Getter
    @Setter
    private LinkedHashSet<String> actions;

    public DataFieldLogic() {
        this.behavior = new HashSet<>();
        this.actions = new LinkedHashSet<>();
    }

    public DataFieldLogic(Set<FieldBehavior> behavior, Set<String> actions) {
        this();
        if(behavior != null) this.behavior.addAll(behavior);
        if(actions != null) this.actions.addAll(actions);
    }

    public ObjectNode applyBehavior(ObjectNode jsonNode){
        behavior.forEach(fieldBehavior -> jsonNode.put(fieldBehavior.toString(),true));
        return jsonNode;
    }

    public ObjectNode applyBehavior(){
        return applyBehavior(JsonNodeFactory.instance.objectNode());
    }

    public void merge(DataFieldLogic other){
        this.behavior.addAll(other.behavior);
        this.actions.addAll(other.actions);
    }

    public boolean isDisplayable(){
        return behavior.contains(FieldBehavior.EDITABLE) || behavior.contains(FieldBehavior.VISIBLE);
    }
}