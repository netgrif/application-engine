package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.importer.model.CompactDirection;
import com.netgrif.application.engine.importer.model.HideEmptyRows;
import com.netgrif.application.engine.importer.model.LayoutType;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.layout.FormLayout;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
public abstract class FormLayoutMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return FormLayout.class;
    }

    @JsonView(Views.Root.class)
    public abstract LayoutType getType();

    @JsonView(Views.Root.class)
    public abstract HideEmptyRows getHideEmptyRows();

    @JsonView(Views.Root.class)
    public abstract CompactDirection getCompactDirection();
}
