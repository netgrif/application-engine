package com.netgrif.application.engine.petrinet.domain.dataset;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.netgrif.application.engine.importer.model.DataEventType;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Imported;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.utils.FieldUtils;
import com.netgrif.application.engine.workflow.domain.DataFieldBehavior;
import com.netgrif.application.engine.workflow.domain.DataFieldBehaviors;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.annotate.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior.*;


@Slf4j
@Document
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BooleanField.class, name = "BOOLEAN"),
        @JsonSubTypes.Type(value = ButtonField.class, name = "BUTTON"),
        @JsonSubTypes.Type(value = CaseField.class, name = "CASE_REF"),
        @JsonSubTypes.Type(value = DateField.class, name = "DATE"),
        @JsonSubTypes.Type(value = DateTimeField.class, name = "DATE_TIME"),
        @JsonSubTypes.Type(value = EnumerationField.class, name = "ENUMERATION"),
        @JsonSubTypes.Type(value = EnumerationMapField.class, name = "ENUMERATION_MAP_FIELD"),
        @JsonSubTypes.Type(value = FileField.class, name = "FILE"),
        @JsonSubTypes.Type(value = FileListField.class, name = "FILE_LIST"),
        @JsonSubTypes.Type(value = FilterField.class, name = "FILTER"),
        @JsonSubTypes.Type(value = I18nField.class, name = "I_18_N"),
        @JsonSubTypes.Type(value = MultichoiceField.class, name = "MULTICHOICE"),
        @JsonSubTypes.Type(value = MultichoiceMapField.class, name = "MULTICHOICE_MAP"),
        @JsonSubTypes.Type(value = NumberField.class, name = "NUMBER"),
        @JsonSubTypes.Type(value = TaskField.class, name = "TASK_REF"),
        @JsonSubTypes.Type(value = TextField.class, name = "TEXT"),
        @JsonSubTypes.Type(value = UserField.class, name = "USER"),
        @JsonSubTypes.Type(value = UserListField.class, name = "USER_LIST"),
})
@EqualsAndHashCode(callSuper = true)
public abstract class Field<T> extends Imported {

    @Id
    protected ObjectId id;
    private I18nString title;
    private I18nString description;
    private I18nString placeholder;
    // TODO: release/8.0.0 dataset or task?
    private DataFieldBehaviors behaviors;
    private DataFieldValue<T> value;
    @JsonIgnore
    protected T staticDefaultValue;
    @JsonIgnore
    protected Expression dynamicDefaultValue;
    protected List<Validation> validations;
    @JsonIgnore
    private boolean immediate;
    @JsonIgnore
    private String encryption;
    private Component component;
    @JsonIgnore
    private Map<DataEventType, DataEvent> events;
    // TODO: release/8.0.0 remove?
    @JsonIgnore
    private Long version = 0L;
    // TODO: release/8.0.0 6.2.5: parentTaskId, parentCaseId
    // TODO: release/8.0.0 unique key map
    private Map<String, String> properties;

    public Field() {
        this.validations = new LinkedList<>();
        this.events = new LinkedHashMap<>();
    }

    public String getStringId() {
        return importId;
    }

    @QueryType(PropertyType.NONE)
    public abstract DataType getType();

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
        return dynamicDefaultValue != null;
    }

    public boolean isNewerThen(Field<?> field) {
        return version > field.version;
    }

    @Override
    public String toString() {
        return title.getDefaultValue();
    }

    public void clone(Field<T> clone) {
        clone.importId = this.importId;
        clone.id = this.id;
//        TODO: release/8.0.0 clone value? events
        // TODO: release/8.0.0 static default value
        if (dynamicDefaultValue != null) {
            clone.dynamicDefaultValue = this.dynamicDefaultValue.clone();
        }
        if (this.validations != null) {
            clone.validations = this.validations.stream().map(Validation::clone).collect(Collectors.toList());
        }
        clone.title = this.title;
        clone.description = this.description;
        clone.placeholder = this.placeholder;
        if (this.behaviors != null) {
            clone.behaviors = this.behaviors.clone();
        }
        clone.immediate = this.immediate;
        if (this.events != null) {
            clone.events = this.events.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone()));
        }
        clone.encryption = this.encryption;
        clone.component = this.component.clone();
    }

    public abstract Field<T> clone();

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

    @JsonIgnore
    public String getTranslatedTitle(Locale locale) {
        if (title == null) {
            return null;
        }
        return title.getTranslation(locale);
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
            // TODO: release/8.0.0 write test on each type of field to check if all properties are cloned
            utils.copyProperties(this, changes);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }
        version++;
    }
}