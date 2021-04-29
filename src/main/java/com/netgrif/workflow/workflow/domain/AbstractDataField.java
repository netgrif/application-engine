package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public abstract class AbstractDataField {
    
    @Getter
    protected Map<String, Set<FieldBehavior>> behavior;

    @Getter
    @Setter
    protected Long version = 0l;

    public AbstractDataField() {
        behavior = new HashMap<>();
    }


    public void setBehavior(Map<String, Set<FieldBehavior>> behavior) {
        this.behavior = behavior;
        update();
    }

    public ObjectNode applyBehavior(String transition, ObjectNode json) {
        behavior.get(transition).forEach(behav -> json.put(behav.toString(), true));
        return json;
    }

    public ObjectNode applyBehavior(String transition) {
        return applyBehavior(transition, JsonNodeFactory.instance.objectNode());
    }

    public void addBehavior(String transition, Set<FieldBehavior> behavior) {
        if (hasDefinedBehavior(transition) && this.behavior.get(transition) != null)
            this.behavior.get(transition).addAll(behavior);
        else
            this.behavior.put(transition, new HashSet<>(behavior));
    }

    public ObjectNode applyOnlyVisibleBehavior(){
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put(FieldBehavior.VISIBLE.toString(),true);
        return node;
    }

    public boolean hasDefinedBehavior(String transition) {
        return this.behavior.containsKey(transition);
    }

    public boolean isDisplayable(String transition) {
        return behavior.containsKey(transition) && (behavior.get(transition).contains(FieldBehavior.VISIBLE) ||
                behavior.get(transition).contains(FieldBehavior.EDITABLE) ||
                behavior.get(transition).contains(FieldBehavior.HIDDEN));
    }

    public boolean isRequired(String transitionId) {
        return behavior.containsKey(transitionId) && behavior.get(transitionId).contains(FieldBehavior.REQUIRED);
    }

    public boolean isVisible(String transitionId) {
        return behavior.containsKey(transitionId) && behavior.get(transitionId).contains(FieldBehavior.VISIBLE);
    }

    public boolean isUndefined(String transitionId) {
        return !behavior.containsKey(transitionId);
    }

    public boolean isDisplayable(){
        return behavior.values().stream().parallel()
                .anyMatch(bs -> bs.contains(FieldBehavior.VISIBLE) || bs.contains(FieldBehavior.EDITABLE) || bs.contains(FieldBehavior.HIDDEN));
    }

    public boolean isForbidden(String transitionId) {
        return behavior.containsKey(transitionId) && behavior.get(transitionId).contains(FieldBehavior.FORBIDDEN);
    }

    public void makeVisible(String transition) {
        changeBehavior(FieldBehavior.VISIBLE, transition);
    }

    public void makeEditable(String transition) {
        changeBehavior(FieldBehavior.EDITABLE, transition);
    }

    public void makeRequired(String transition) {
        changeBehavior(FieldBehavior.REQUIRED, transition);
    }

    public void makeOptional(String transition) {
        changeBehavior(FieldBehavior.OPTIONAL, transition);
    }

    public void makeHidden(String transition) {
        changeBehavior(FieldBehavior.HIDDEN, transition);
    }

    public void makeForbidden(String transition) {
        changeBehavior(FieldBehavior.FORBIDDEN, transition);
    }

    protected void changeBehavior(FieldBehavior behavior, String transition) {
        List<FieldBehavior> tmp = Arrays.asList(behavior.getAntonyms());
        tmp.forEach(beh -> this.behavior.get(transition).remove(beh));
        this.behavior.get(transition).add(behavior);
        update();
    }

    protected void update() {
        version++;
    }

    public boolean isNewerThen(DataField other) {
        return version > other.getVersion();
    }
    
}
