package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import org.springframework.beans.factory.annotation.Lookup;

import java.util.List;

@NaeMixin
public abstract class GetDataGroupsEventOutcomeMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return GetDataGroupsEventOutcome.class;
    }

    @JsonView(Views.GetData.class)
    public abstract List<DataGroup> getData();
}
