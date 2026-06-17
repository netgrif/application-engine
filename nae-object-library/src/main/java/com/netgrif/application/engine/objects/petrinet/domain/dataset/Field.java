package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.application.engine.objects.petrinet.domain.Component;
import com.netgrif.application.engine.objects.petrinet.domain.Format;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.Imported;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.runner.Expression;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.objects.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.objects.petrinet.domain.events.DataEventType;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Field<T> extends Imported implements Serializable {

    @Getter
    @Serial
    private static final long serialVersionUID = 8315043110342747937L;
    @Setter
    @Getter
    protected ObjectId _id;
    @Setter
    @Getter
    private I18nString name;
    @Setter
    @Getter
    private I18nString description;
    @Setter
    @Getter
    private I18nString placeholder;
    @Setter
    @Getter
    private ObjectNode behavior;
    @Setter
    @Getter
    private FieldLayout layout;
    @Setter
    @Getter
    private T value;
    @Setter
    @Getter
    private Long order;
    @JsonIgnore
    private boolean immediate;
    @Setter
    @Getter
    @JsonIgnore
    private Map<DataEventType, DataEvent> events;
    @Setter
    @Getter
    @JsonIgnore
    private String encryption;
    @Setter
    @Getter
    private Format format;
    @Setter
    @Getter
    private Integer length;
    @Setter
    @Getter
    private Component component;
    @Setter
    @Getter
    protected T defaultValue;
    @Setter
    @Getter
    protected Expression initExpression;
    @Setter
    @Getter
    protected List<Validation> validations;
    @Setter
    @Getter
    protected String parentTaskId;
    @Setter
    @Getter
    protected String parentCaseId;

    public Field() {
        _id = new ObjectId();
        this.name = new I18nString();
        this.description = new I18nString();
        this.placeholder = new I18nString();
        this.events = new HashMap<>();
        this.validations = new ArrayList<>();
    }

    public Field(String importId) {
        this();
        this.setImportId(importId);
    }

    public String getStringId() {
        return getImportId();
    }

    public abstract FieldType getType();

    public Boolean isImmediate() {
        return immediate;
    }

    public void setImmediate(Boolean immediate) {
        this.immediate = immediate != null && immediate;
    }

    public void addActions(Collection<Action> dataEvents, final DataEventType type) {
        dataEvents.forEach(it -> addAction(it, type));
    }

    public void addAction(Action action, DataEventType type) {
        if (action == null) return;
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
            this.validations = new ArrayList<Validation>();
        }

        this.validations.add(validation);
    }

    public void clearValue() {
        setValue(null);
    }

    public boolean hasDefault() {
        return defaultValue != null || initExpression != null;
    }

    public String getTranslatedName(Locale locale) {
        return name.getTranslation(locale);
    }

    public String getTranslatedPlaceholder(Locale locale) {
        return placeholder.getTranslation(locale);
    }

    public String getTranslatedDescription(Locale locale) {
        return description.getTranslation(locale);
    }

    @Override
    public String toString() {
        return name.getDefaultValue();
    }

    public abstract Field<?> clone();

    public void clone(Field<T> clone) {
        clone.set_id(this._id);
        clone.setImportId(this.getImportId());
        clone.setName(this.name);
        clone.setDescription(this.description);
        clone.setPlaceholder(this.placeholder);
        clone.setOrder(this.order);
        clone.setImmediate(this.immediate);
        clone.setEvents(this.events);
        clone.setEncryption(this.encryption);
        clone.setFormat(this.format);
        clone.setLength(this.length);
        clone.setComponent(this.component);
        clone.setValidations(this.validations.stream().map(Validation::clone).collect(Collectors.toList()));
        clone.setDefaultValue(this.defaultValue);
        clone.setInitExpression(this.initExpression);
    }
}
