package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldWithAllowedNets;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.List;

@NaeMixin
public abstract class FieldWithAllowedNetsMixin<T> extends FieldMixin<T> {

    @Lookup
    public static Class<?> getOriginalType() {
        return FieldWithAllowedNets.class;
    }

    @JsonView(Views.GetData.class)
    public abstract List<String> getAllowedNets();
}
