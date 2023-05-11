package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.converter.I18nStringSerializer;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
@JsonSerialize(using = I18nStringSerializer.class)
public abstract class I18nStringMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return I18nString.class;
    }
}
