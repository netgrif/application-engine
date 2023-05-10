package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.hateoas.EntityModel;

@NaeMixin
public abstract class EntityModelMixin<T> {

    @Lookup
    public static Class<?> getOriginalType() {
        return EntityModel.class;
    }

    @JsonView(Views.Root.class)
    public abstract T getContent();
}
