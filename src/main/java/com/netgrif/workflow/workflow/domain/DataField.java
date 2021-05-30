package com.netgrif.workflow.workflow.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.importer.model.Validations;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.Validation;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataField extends AbstractDataField {

    @Getter
    private Object value;

    @Getter
    protected Set<I18nString> choices;

    @Getter
    protected List<String> allowedNets;

    @Getter
    protected Map<String, I18nString> options;

    @Getter
    private List<Validation> validations;

    @Getter
    private Map<String, Object> filterMetadata;

    @Getter
    @Setter
    @JsonIgnore
    protected String encryption;

    public DataField() {
        super();
    }

    public DataField(Object value) {
        this();
        this.value = value;
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
}