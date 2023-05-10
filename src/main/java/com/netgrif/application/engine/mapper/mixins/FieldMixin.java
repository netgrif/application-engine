package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.List;

@NaeMixin
public abstract class FieldMixin<T> {

    public FieldMixin() {}

    @Lookup
    public static Class<?> getOriginalType() {
        return Field.class;
    }

    @JsonView(Views.GetData.class)
    public abstract String getId();

    @JsonView(Views.GetData.class)
    public abstract List<Validation> getValidations();

    @JsonView(Views.GetData.class)
    public abstract I18nString getName();

    @JsonView(Views.GetData.class)
    public abstract I18nString getDescription();

    @JsonView(Views.GetData.class)
    public abstract I18nString getPlaceholder();

    @JsonView(Views.GetData.class)
    public abstract DataFieldValue<T> getValue();

    @JsonView(Views.GetData.class)
    public abstract Integer getLength();

    @JsonView(Views.GetData.class)
    public abstract Component getComponent();

    @JsonView(Views.GetData.class)
    public abstract DataType getType();
}
