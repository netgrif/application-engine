package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.model.Expression;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.converter.DataFieldValueSerializer;
import com.netgrif.application.engine.mapper.filters.DataFieldBehaviorsFilter;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.DataEventType;
import com.netgrif.application.engine.workflow.domain.DataFieldBehaviors;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@NaeMixin
public abstract class FieldMixin<T> {

    @Lookup
    public static Class<?> getOriginalType() {
        return Field.class;
    }

    @JsonView(Views.GetData.class)
    public abstract String getId();

    @JsonIgnore
    public abstract T getDefaultValue();

    @JsonIgnore
    public abstract Expression getExpression();

    @JsonView(Views.GetData.class)
    public abstract List<Validation> getValidations();

    @JsonView(Views.GetData.class)
    public abstract I18nString getName();

    @JsonView(Views.GetData.class)
    public abstract I18nString getDescription();

    @JsonView(Views.GetData.class)
    public abstract I18nString getPlaceholder();

    @JsonView(Views.GetData.class)
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DataFieldBehaviorsFilter.class)
    public abstract DataFieldBehaviors getBehaviors();

    @JsonView(Views.GetData.class)
    @JsonSerialize(using = DataFieldValueSerializer.class)
    public abstract DataFieldValue<T> getValue();

    @JsonIgnore
    public abstract Boolean isImmediate();

    @JsonIgnore
    public abstract Map<DataEventType, DataEvent> getEvents();

    @JsonIgnore
    public abstract String getEncryption();

    @JsonIgnore
    public abstract Long getVersion();

    @JsonView(Views.GetData.class)
    public abstract Integer getLength();

    @JsonView(Views.GetData.class)
    public abstract Component getComponent();

    @JsonView(Views.GetData.class)
    public abstract DataType getType();

    @JsonView(Views.GetData.class)
    public abstract String getStringId();

    @JsonIgnore
    public abstract boolean isDynamicDefaultValue();

    @JsonIgnore
    public abstract String getTranslatedName(Locale locale);
}
