package com.netgrif.application.engine.workflow.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.arcs.reference.Referencable;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DataField implements Referencable {

    @Getter
    private Map<String, Set<FieldBehavior>> behavior;

    @Getter
    private Object value;

    @Getter
    private Set<I18nString> choices;

    @Getter
    private List<String> allowedNets;

    @Getter
    private Map<String, I18nString> options;

    @Getter
    private List<Validation> validations;

    @Getter
    private Map<String, Object> filterMetadata;

    @Getter
    @Setter
    @JsonIgnore
    private String encryption;

    @Getter
    @Setter
    private LocalDateTime lastModified;

    @Getter
    @Setter
    private Long version = 0l;

    public DataField() {
        behavior = new HashMap<>();
    }

    public DataField(Object value) {
        this();
        this.value = value;
    }

    public void setBehavior(Map<String, Set<FieldBehavior>> behavior) {
        this.behavior = behavior;
        update();
    }

    public void setValue(Object value) {
        this.value = value;
        update();
    }

    public void setChoices(Set<I18nString> choices) {
        this.choices = choices;
        update();
    }

    public void setAllowedNets(List<String> allowedNets) {
        this.allowedNets = allowedNets;
        update();
    }

    public void setFilterMetadata(Map<String, Object> filterMetadata) {
        this.filterMetadata = filterMetadata;
        update();
    }

    public void setOptions(Map<String, I18nString> options) {
        this.options = options;
        update();
    }

    public void setValidations(List<Validation> validations) {
        this.validations = validations;
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

    public ObjectNode applyOnlyVisibleBehavior() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put(FieldBehavior.VISIBLE.toString(), true);
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

    public boolean isDisplayable() {
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

    private void changeBehavior(FieldBehavior behavior, String transition) {
        List<FieldBehavior> tmp = Arrays.asList(behavior.getAntonyms());
        tmp.forEach(beh -> this.behavior.get(transition).remove(beh));
        this.behavior.get(transition).add(behavior);
        update();
    }

    private void update() {
        version++;
    }

    public boolean isNewerThen(DataField other) {
        return version > other.getVersion();
    }

    @QueryType(PropertyType.STRING)
    String getStringValue() {
        if (value == null)
            return "";
        return value.toString();
    }

    @Override
    public String toString() {
        if (value == null)
            return "null";
        return value.toString();
    }

    @Override
    public int getMultiplicity() {
        double parsedValue = Double.parseDouble(String.valueOf(value));
        if(parsedValue == Math.floor(parsedValue) && !Double.isInfinite(parsedValue)){
            return (int) Double.parseDouble(String.valueOf(value));
        } else {
            throw new IllegalArgumentException("Variable arc must be an non negative integer");
        }
    }
}