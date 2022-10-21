package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Imported;
import com.netgrif.application.engine.petrinet.domain.arcs.reference.Referencable;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.Expression;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.DataEventType;
import com.netgrif.application.engine.workflow.domain.DataFieldBehavior;
import com.netgrif.application.engine.workflow.domain.DataFieldBehaviors;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Document
@Data
public abstract class Field<T> extends Imported implements Referencable {

    @Id
    protected ObjectId _id;
    protected T defaultValue;
    protected Expression initExpression;
    protected List<Validation> validations;
    private I18nString name;
    private I18nString description;
    private I18nString placeholder;
    private DataFieldBehaviors behaviors;
    private DataFieldValue<T> value;
    private Long order;
    //TODO: NAE-1645 jsonignore?
    private boolean immediate;
    private Map<DataEventType, DataEvent> events;
    private String encryption;
    private Integer length;
    private Component component;
    private Long version = 0L;

    public Field() {
        _id = new ObjectId();
        this.events = new HashMap<>();
    }

    public String getStringId() {
        return importId;
    }

    @QueryType(PropertyType.NONE)
    public abstract DataType getType();

    public void setValue(T value) {
        if (this.value == null) {
            this.value = new DataFieldValue<>();
        }
        this.value.setValue(value);
    }

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

    public void applyChanges(Field<?> field) {
        // TODO: NAE-1645
    }

    public boolean isNewerThen(Field<?> field) {
        // TODO: NAE-1645
        return true;
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
        clone.length = this.length;
        clone.component = this.component;
        if (this.validations != null) {
            clone.validations = this.validations.stream().map(Validation::clone).collect(Collectors.toList());
        }
        clone.defaultValue = this.defaultValue;
        clone.initExpression = this.initExpression;
    }

    public abstract Field<T> clone();

    @Override
    public int getMultiplicity() {
        throw new UnsupportedOperationException(this.getClass().toString() + " can not be used as arc multiplicity");
    }

    public boolean isForbidden(String transitionId) {
        return isBehavior(transitionId, dataFieldBehavior -> dataFieldBehavior.getBehavior() == FieldBehavior.FORBIDDEN);
    }

    public boolean isDisplayable(String transitionId) {
        return isBehavior(transitionId, dataFieldBehavior -> dataFieldBehavior.getBehavior() == FieldBehavior.VISIBLE || dataFieldBehavior.getBehavior() == FieldBehavior.EDITABLE);
    }

    private boolean isBehavior(String transitionId, Function<DataFieldBehavior, Boolean> compare) {
        if (behaviors == null) {
            return false;
        }
        DataFieldBehavior fieldBehavior = behaviors.get(transitionId);
        if (fieldBehavior == null) {
            return false;
        }
        return compare.apply(fieldBehavior);
    }
}