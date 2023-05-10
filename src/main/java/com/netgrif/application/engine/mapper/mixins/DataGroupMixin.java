package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.importer.model.DataGroupAlignment;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.DataRef;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.layout.DataGroupLayout;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.LinkedHashMap;

@NaeMixin
public abstract class DataGroupMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return DataGroup.class;
    }

    @JsonView(Views.GetData.class)
    public abstract LinkedHashMap<String, DataRef> getDataRefs();

    @JsonView(Views.GetData.class)
    public abstract DataGroupLayout getLayout();

    @JsonView(Views.GetData.class)
    public abstract I18nString getTitle();

    @JsonView(Views.GetData.class)
    public abstract DataGroupAlignment getAlignment();

    @JsonView(Views.GetData.class)
    public abstract Boolean isStretch();

    @JsonView(Views.GetData.class)
    public abstract String getParentTaskId();

    @JsonView(Views.GetData.class)
    public abstract String getParentTransitionId();

    @JsonView(Views.GetData.class)
    public abstract String getParentCaseId();

    @JsonView(Views.GetData.class)
    public abstract String getParentTaskRefId();

    @JsonView(Views.GetData.class)
    public abstract int getNestingLevel();
}
