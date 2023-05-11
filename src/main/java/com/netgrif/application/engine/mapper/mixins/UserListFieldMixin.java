package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.Set;

@NaeMixin
public abstract class UserListFieldMixin extends FieldMixin<UserListFieldValue> {

    @Lookup
    public static Class<?> getOriginalType() {
        return UserListField.class;
    }

    @JsonView(Views.GetData.class)
    public abstract Set<String> getRoles();
}
