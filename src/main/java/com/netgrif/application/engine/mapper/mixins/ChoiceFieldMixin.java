package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.ChoiceField;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.Expression;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.Set;

@NaeMixin
public abstract class ChoiceFieldMixin<T> extends Field<T> {

    @Lookup
    public static Class<?> getOriginalType() {
        return ChoiceField.class;
    }

    @JsonView(Views.GetData.class)
    public abstract Set<I18nString> getChoices();

    @JsonView(Views.GetData.class)
    public abstract Expression getChoicesExpression();
}
