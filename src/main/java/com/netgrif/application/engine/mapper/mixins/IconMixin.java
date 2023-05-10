package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.Icon;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
public abstract class IconMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return Icon.class;
    }

    @JsonView(Views.Root.class)
    public abstract String getKey();

    @JsonView(Views.Root.class)
    public abstract String getValue();

    @JsonView(Views.Root.class)
    public abstract String getType();
}
