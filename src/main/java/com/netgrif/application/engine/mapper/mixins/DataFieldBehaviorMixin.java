package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.workflow.domain.DataFieldBehavior;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
public abstract class DataFieldBehaviorMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return DataFieldBehavior.class;
    }

    @JsonView(Views.Root.class)
    public abstract boolean isForbidden();

    @JsonView(Views.Root.class)
    public abstract boolean isEditable();

    @JsonView(Views.Root.class)
    public abstract boolean isHidden();

    @JsonView(Views.Root.class)
    public abstract boolean isVisible();

    @JsonView(Views.Root.class)
    public abstract boolean isRequired();

    @JsonView(Views.Root.class)
    public abstract boolean isImmediate();
}
