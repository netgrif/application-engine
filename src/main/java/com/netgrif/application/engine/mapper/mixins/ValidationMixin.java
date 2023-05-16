package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
public abstract class ValidationMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return Validation.class;
    }

    @JsonView(Views.Root.class)
    public abstract String getValidationRule();

    @JsonView(Views.Root.class)
    public abstract I18nString getValidationMessage();
}
