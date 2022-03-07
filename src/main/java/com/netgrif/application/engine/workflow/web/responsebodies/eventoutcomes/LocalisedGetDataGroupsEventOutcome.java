package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.DataGroup;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LocalisedGetDataGroupsEventOutcome extends LocalisedTaskEventOutcome {

    private List<DataGroup> data;

    public LocalisedGetDataGroupsEventOutcome(GetDataGroupsEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.data = outcome.getData().stream()
                .map(dg -> {
                    DataGroup dataGroup = new DataGroup(dg.getFields(), dg.getTranslatedTitle(locale), dg.getAlignment(), dg.getStretch(), dg.getLayout(), dg.getParentTaskId(), dg.getParentCaseId(), dg.getParentTaskRefId(), dg.getNestingLevel());
                    dataGroup.setParentTransitionId(dg.getParentTransitionId());
                    return dataGroup;
                })
                .collect(Collectors.toList());
    }

    public List<DataGroup> getData() {
        return data;
    }
}
