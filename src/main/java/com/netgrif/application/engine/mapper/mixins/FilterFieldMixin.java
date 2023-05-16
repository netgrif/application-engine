package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.dataset.FilterField;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.Map;

@NaeMixin
public abstract class FilterFieldMixin extends FieldWithAllowedNetsMixin<String> {

    @Lookup
    public static Class<?> getOriginalType() {
        return FilterField.class;
    }

    @JsonView(Views.GetData.class)
    public abstract Map<String, Object> getFilterMetadata();
}
