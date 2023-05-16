package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.importer.model.Component;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.DataRef;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.application.engine.workflow.domain.DataFieldBehavior;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
public abstract class DataRefMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return DataRef.class;
    }

    @JsonView(Views.GetData.class)
    public abstract String getFieldId();

    @JsonView(Views.GetData.class)
    public abstract Field<?> getField();

    @JsonView(Views.GetData.class)
    public abstract DataFieldBehavior getBehavior();

    @JsonView(Views.GetData.class)
    public abstract FieldLayout getLayout();

    @JsonView(Views.GetData.class)
    public abstract Component getComponent();

    @JsonView(Views.GetData.class)
    public abstract String getParentTaskId();

    @JsonView(Views.GetData.class)
    public abstract String getParentCaseId();

}
