package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.importer.model.Expression;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.DynamicValidation;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
public abstract class DynamicValidationMixin extends ValidationMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return DynamicValidation.class;
    }

    @JsonView(Views.Root.class)
    public abstract String getCompiledRule();

    @JsonView(Views.Root.class)
    public abstract Expression getExpression();
}
