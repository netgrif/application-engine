package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.layout.Layout;
import org.springframework.beans.factory.annotation.Lookup;

public abstract class LayoutMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return Layout.class;
    }

    @JsonView(Views.Root.class)
    public abstract Integer getRows();

    @JsonView(Views.Root.class)
    public abstract Integer getCols();
}
