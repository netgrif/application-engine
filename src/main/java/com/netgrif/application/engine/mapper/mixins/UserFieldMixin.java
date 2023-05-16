package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.dataset.UserField;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.Set;

@NaeMixin
public abstract class UserFieldMixin extends FieldMixin<UserFieldValue> {

    @Lookup
    public static Class<?> getOriginalType() {
        return UserField.class;
    }

    @JsonView(Views.GetData.class)
    public abstract Set<String> getRoles();
}
