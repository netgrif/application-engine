package com.netgrif.application.engine.petrinet.domain.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.Format;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Imported;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.Expression;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.DataEventType;
import com.netgrif.application.engine.petrinet.domain.views.View;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.stream.Collectors;

@Document
@Data
public abstract class Field<T> extends Imported {

    @Id
    protected ObjectId _id;
    protected T defaultValue;
    protected Expression initExpression;
    protected List<Validation> validations;
    @Transient
    protected String parentTaskId;
    @Transient
    protected String parentCaseId;
    private I18nString name;
    private I18nString description;
    private I18nString placeholder;
    @Transient
    private Set<FieldBehavior> behavior;
    @Transient
    private FieldLayout layout;
    @Transient
    private T value;
    private Long order;
    @JsonIgnore
    private boolean immediate;
    @JsonIgnore
    private Map<DataEventType, DataEvent> events;
    @JsonIgnore
    private String encryption;
    private Format format;
    private View view;
    private Integer length;
    private Component component;

    public Field() {
        _id = new ObjectId();
        this.events = new HashMap<>();
    }

    public String getStringId() {
        return importId;
    }

    @QueryType(PropertyType.NONE)
    public abstract DataType getType();

    public void addActions(Collection<Action> dataEvents, DataEventType type) {
        dataEvents.forEach(action -> addAction(action, type));
    }

    public void addAction(Action action, DataEventType type) {
        if (action == null) {
            return;
        }
        if (this.events == null) {
            this.events = new HashMap<>();
        }
        if (!this.events.containsKey(type)) {
            this.events.get(type).addToActionsByDefaultPhase(action);
        } else {
            DataEvent event = new DataEvent();
            event.setId(new ObjectId().toString());
            event.setType(type);
            event.addToActionsByDefaultPhase(action);
            this.events.put(type, event);
        }
    }

    public boolean isDynamicDefaultValue() {
        return initExpression != null;
    }

    public void addValidation(Validation validation) {
        if (validations == null) {
            this.validations = new ArrayList<>();
        }
        this.validations.add(validation);
    }

    public List<Validation> getValidations() {
        return validations;
    }

    public void setValidations(List<Validation> validations) {
        this.validations = validations;
    }

    public void clearValue() {
        this.value = null;
    }

    public boolean hasDefault() {
        return defaultValue != null || initExpression != null;
    }

    public String getTranslatedName(Locale locale) {
        if (name == null) {
            return null;
        }
        return name.getTranslation(locale);
    }

    public String getTranslatedPlaceholder(Locale locale) {
        if (placeholder == null) {
            return null;
        }
        return placeholder.getTranslation(locale);
    }

    public String getTranslatedDescription(Locale locale) {
        if (description == null) {
            return null;
        }
        return description.getTranslation(locale);
    }

    @Override
    public String toString() {
        return name.getDefaultValue();
    }

    public void clone(Field<T> clone) {
        clone._id = this._id;
        clone.importId = this.importId;
        clone.name = this.name;
        clone.description = this.description;
        clone.placeholder = this.placeholder;
        clone.order = this.order;
        clone.immediate = this.immediate;
        clone.events = this.events;
        clone.encryption = this.encryption;
        clone.view = this.view;
        clone.format = this.format;
        clone.length = this.length;
        clone.component = this.component;
        if (this.validations != null) {
            clone.validations = this.validations.stream().map(Validation::clone).collect(Collectors.toList());
        }
        clone.defaultValue = this.defaultValue;
        clone.initExpression = this.initExpression;
    }

    public abstract Field<T> clone();
}