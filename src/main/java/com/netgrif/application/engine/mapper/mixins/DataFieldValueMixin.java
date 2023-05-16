package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
public abstract class DataFieldValueMixin<T> {

    @Lookup
    public static Class<?> getOriginalType() {
        return DataFieldValue.class;
    }

    @JsonView(Views.Root.class)
    public abstract T getValue();
}
