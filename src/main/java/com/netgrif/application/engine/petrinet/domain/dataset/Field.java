package com.netgrif.application.engine.petrinet.domain.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.mapper.filters.DataFieldBehaviorsFilter;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Imported;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.Expression;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.DataEventType;
import com.netgrif.application.engine.utils.FieldUtils;
import com.netgrif.application.engine.workflow.domain.DataFieldBehavior;
import com.netgrif.application.engine.workflow.domain.DataFieldBehaviors;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior.*;

@Slf4j
@Document
@Data
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NumberField.class, name = "0"),
        @JsonSubTypes.Type(value = TextField.class, name = "1"),
        @JsonSubTypes.Type(value = EnumerationField.class, name = "2"),
        @JsonSubTypes.Type(value = EnumerationMapField.class, name = "3"),
        @JsonSubTypes.Type(value = MultichoiceField.class, name = "4"),
        @JsonSubTypes.Type(value = MultichoiceMapField.class, name = "5"),
        @JsonSubTypes.Type(value = BooleanField.class, name = "6"),
        @JsonSubTypes.Type(value = DateField.class, name = "7"),
        @JsonSubTypes.Type(value = FileField.class, name = "8"),
        @JsonSubTypes.Type(value = FileListField.class, name = "9"),
        @JsonSubTypes.Type(value = UserField.class, name = "10"),
        @JsonSubTypes.Type(value = UserListField.class, name = "11"),
        @JsonSubTypes.Type(value = DateTimeField.class, name = "12"),
        @JsonSubTypes.Type(value = ButtonField.class, name = "13"),
        @JsonSubTypes.Type(value = TaskField.class, name = "14"),
        @JsonSubTypes.Type(value = CaseField.class, name = "15"),
        @JsonSubTypes.Type(value = FilterField.class, name = "16"),
        @JsonSubTypes.Type(value = I18nField.class, name = "17"),
})
public class Field<T> extends Imported {

    @Id
    protected ObjectId id;
    protected T defaultValue;
    protected Expression initExpression;
    protected List<Validation> validations;
    private I18nString name;
    private I18nString description;
    private I18nString placeholder;
    private DataFieldBehaviors behaviors;
    private DataFieldValue<T> value;
    private Boolean immediate;
    private Map<DataEventType, DataEvent> events;
    private String encryption;
    private Integer length;
    private Component component;
    private Long version = 0L;
    // TODO: release/7.0.0 6.2.5: parentTaskId, parentCaseId

    public String getId() {
        if (id == null) {
            return null;
        }
        return id.toString();
    }

    public String getStringId() {
        return importId;
    }

    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.DATE;
    };

    public void setRawValue(T value) {
        if (this.value == null) {
            this.value = new DataFieldValue<>();
        }
        this.value.setValue(value);
    }

    public T getRawValue() {
        if (this.value == null) {
            return null;
        }
        return this.value.getValue();
    }

    public boolean isImmediate() {
        return this.immediate != null && this.immediate;
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

    public void clearValue() {
        this.value = null;
    }

    public boolean hasDefault() {
        return defaultValue != null || initExpression != null;
    }

    public boolean isNewerThen(Field<?> field) {
        return version > field.version;
    }

    @Override
    public String toString() {
        return name.getDefaultValue();
    }

    public void clone(Field<T> clone) {
        clone.importId = this.importId;
        clone.id = this.id;
        clone.defaultValue = this.defaultValue;
        if (this.initExpression != null) {
            clone.initExpression = this.initExpression.clone();
        }
        if (this.validations != null) {
            clone.validations = this.validations.stream().map(Validation::clone).collect(Collectors.toList());
        }
        clone.name = this.name;
        clone.description = this.description;
        clone.placeholder = this.placeholder;
        if (this.behaviors != null) {
            clone.behaviors = this.behaviors.clone();
        }
//        TODO: release/7.0.0 clone value? events
//        if (this.value != null) {
//            clone.value = this.value.clone();
//        }
        clone.immediate = this.immediate;
        if (this.events != null) {
            clone.events = this.events.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone()));
        }
        clone.encryption = this.encryption;
        clone.length = this.length;
        clone.component = this.component;
    }

    public  Field<T> clone() {
        Field field = new Field();
        clone(field);
        return field;
    }

    public void setBehavior(String transitionId, DataFieldBehavior behavior) {
        behaviors.put(transitionId, behavior);
    }

    public boolean isForbiddenOn(String transitionId) {
        return isBehaviorSetOn(transitionId, FORBIDDEN);
    }

    public boolean isEditableOn(String transitionId) {
        return isBehaviorSetOn(transitionId, EDITABLE);
    }

    public boolean isHiddenOn(String transitionId) {
        return isBehaviorSetOn(transitionId, HIDDEN);
    }

    public boolean isVisibleOn(String transitionId) {
        return isBehaviorSetOn(transitionId, VISIBLE);
    }

    private boolean isBehaviorSetOn(String transitionId, FieldBehavior behavior) {
        DataFieldBehavior dataRefBehavior = behaviors.get(transitionId);
        if (dataRefBehavior == null) {
            return false;
        }
        return behavior.equals(dataRefBehavior.getBehavior());
    }

    public String getTranslatedName(Locale locale) {
        if (name == null) {
            return null;
        }
        return name.getTranslation(locale);
    }

    /**
     * Replace all attributes with non-null
     */
    public void applyChanges(Field<?> changes) {
        if (changes == null) {
            return;
        }
        try {
            FieldUtils utils = new FieldUtils();
            // TODO: release/7.0.0 write test on each type of field to check if all properties are cloned
            utils.copyProperties(this, changes);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }
        version++;
    }

    public void applyDefaultValue() {
        this.setRawValue(this.getDefaultValue());
    }
}