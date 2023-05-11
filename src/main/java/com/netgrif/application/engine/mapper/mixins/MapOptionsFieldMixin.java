package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.Expression;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.Map;

@NaeMixin
public abstract class MapOptionsFieldMixin<T, U> extends FieldMixin<U> {

    @Lookup
    public static Class<?> getOriginalType() {
        return MapOptionsField.class;
    }

    @JsonView(Views.GetData.class)
    public abstract Map<String, T> getOptions();

    @JsonView(Views.GetData.class)
    public abstract Expression getOptionsExpression();
}
