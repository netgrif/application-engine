package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.Icon;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.List;
import java.util.Map;


@NaeMixin
public abstract class ComponentMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return Component.class;
    }

    @JsonView(Views.Root.class)
    public abstract String getName();

    @JsonView(Views.Root.class)
    public abstract Map<String, String> getProperties();

    @JsonView(Views.Root.class)
    public abstract List<Icon> getOptionIcons();
}
