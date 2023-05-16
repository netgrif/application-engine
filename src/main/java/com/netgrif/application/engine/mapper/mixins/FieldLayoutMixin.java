package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldLayout;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
public abstract class FieldLayoutMixin extends LayoutMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return FieldLayout.class;
    }

    @JsonView(Views.Root.class)
    public abstract int getX();

    @JsonView(Views.Root.class)
    public abstract int getY();

    @JsonView(Views.Root.class)
    public abstract int getOffset();

    @JsonView(Views.Root.class)
    public abstract String getTemplate();

    @JsonView(Views.Root.class)
    public abstract String getAppearance();

    @JsonView(Views.Root.class)
    public abstract String getAlignment();
}
