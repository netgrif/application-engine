package com.netgrif.application.engine.mapper.mixins;

import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.petrinet.domain.layout.DataGroupLayout;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
public abstract class DataGroupLayoutMixin extends FormLayoutMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return DataGroupLayout.class;
    }
}
