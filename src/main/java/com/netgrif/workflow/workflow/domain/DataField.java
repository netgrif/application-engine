package com.netgrif.workflow.workflow.domain;


import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataField {

    @Getter @Setter
    private Map<String, Set<FieldBehavior>> behavior;

    @Getter @Setter
    private Object value;

    public DataField() {
        behavior = new HashMap<>();
    }

    public DataField(Object value) {
        this();
        this.value = value;
    }

    public ObjectNode applyBehavior(String transition, ObjectNode json){
        behavior.get(transition).forEach(behav -> json.put(behav.toString(),true));
        return json;
    }

    public ObjectNode applyBehavior(String transition){
        return applyBehavior(transition, JsonNodeFactory.instance.objectNode());
    }

    public void addBehavior(String transition, Set<FieldBehavior> behavior){
        if(hasDefinedBehavior(transition) && this.behavior.get(transition) != null)
            this.behavior.get(transition).addAll(behavior);
        else
            this.behavior.put(transition, new HashSet<>(behavior));
    }

    public boolean hasDefinedBehavior(String transition){
        return this.behavior.containsKey(transition);
    }

    public void makeVisible(String transition){
        this.behavior.get(transition).remove(FieldBehavior.EDITABLE);
        this.behavior.get(transition).add(FieldBehavior.VISIBLE);
    }

    public void makeEditable(String transition){
        this.behavior.get(transition).remove(FieldBehavior.VISIBLE);
        this.behavior.get(transition).add(FieldBehavior.EDITABLE);
    }

    public void makeRequired(String transition){
        this.behavior.get(transition).remove(FieldBehavior.OPTIONAL);
        this.behavior.get(transition).add(FieldBehavior.REQUIRED);
    }

    public void makeOptional(String transition){
        this.behavior.get(transition).remove(FieldBehavior.REQUIRED);
        this.behavior.get(transition).add(FieldBehavior.OPTIONAL);
    }
}
