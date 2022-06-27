package com.netgrif.application.engine.workflow.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.arcs.reference.Referencable;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataField implements Referencable {

    @Getter
    private DataFieldBehavior behavior;

    private DataFieldValue value;

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
    private Long version = 0L;

    public DataField() {
        behavior = new DataFieldBehavior();
    }

    public DataField(Object value) {
        this();
        this.value = new DataFieldValue(value);
    }

    public void setBehavior(Map<String, Set<FieldBehavior>> behavior) {
        this.behavior = new DataFieldBehavior(behavior);
        update();
    }

    public Object getValue() {
        if (value == null) {
            return null;
        }
        return value.getValue();
    }

    public void setValue(Object value) {
        if (this.value == null) {
            this.value = new DataFieldValue();
        }
        this.value.setValue(value);
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

    public void addBehavior(String transition, Set<FieldBehavior> behavior) {
        if (hasDefinedBehavior(transition) && this.behavior.get(transition) != null)
            this.behavior.get(transition).addAll(behavior);
        else
            this.behavior.put(transition, new HashSet<>(behavior));
    }

    public boolean hasDefinedBehavior(String transition) {
        return this.behavior.contains(transition);
    }

    public boolean isDisplayable(String transition) {
        return behavior.contains(transition) && (behavior.get(transition).contains(FieldBehavior.VISIBLE) ||
                behavior.get(transition).contains(FieldBehavior.EDITABLE) ||
                behavior.get(transition).contains(FieldBehavior.HIDDEN));
    }

    public boolean isRequired(String transitionId) {
        return behavior.contains(transitionId) && behavior.get(transitionId).contains(FieldBehavior.REQUIRED);
    }

    public boolean isVisible(String transitionId) {
        return behavior.contains(transitionId) && behavior.get(transitionId).contains(FieldBehavior.VISIBLE);
    }

    public boolean isUndefined(String transitionId) {
        return !behavior.contains(transitionId);
    }

    public boolean isDisplayable() {
        return behavior.getBehaviors().values().stream().anyMatch(bs -> bs.contains(FieldBehavior.VISIBLE) || bs.contains(FieldBehavior.EDITABLE) || bs.contains(FieldBehavior.HIDDEN));
    }

    public boolean isForbidden(String transitionId) {
        return behavior.contains(transitionId) && behavior.get(transitionId).contains(FieldBehavior.FORBIDDEN);
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
        List<FieldBehavior> tmp = behavior.getAntonyms();
        tmp.forEach(beh -> this.behavior.get(transition).remove(beh));
        this.behavior.get(transition).add(behavior);
        update();
    }

    public void applyChanges(DataField newVersion) {
        this.value = newVersion.value;
        newVersion.behavior.getBehaviors().forEach((transitionId, behaviors) -> {
            behaviors.forEach(behavior -> {
                this.changeBehavior(behavior, transitionId);
            });
        });
        this.choices = newVersion.choices; // TODO: NAE-1645 copy?
        this.allowedNets = newVersion.allowedNets;
        this.options = newVersion.options;
        this.validations = newVersion.validations;
        this.filterMetadata = newVersion.filterMetadata;
        this.encryption = newVersion.encryption;
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
        if (parsedValue == Math.floor(parsedValue) && !Double.isInfinite(parsedValue)) {
            return (int) Double.parseDouble(String.valueOf(value));
        } else {
            throw new IllegalArgumentException("Variable arc must be an non negative integer");
        }
    }
}