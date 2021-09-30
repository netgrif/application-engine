package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.DataGroup;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LocalisedGetDataGroupsEventOutcome extends LocalisedTaskEventOutcome {

    private List<DataGroup> data;

    public LocalisedGetDataGroupsEventOutcome(GetDataGroupsEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.data = outcome.getData().stream()
                .map(dg -> new DataGroup(dg.getFields(), dg.getTranslatedTitle(locale), dg.getAlignment(), dg.getStretch(), dg.getLayout(), dg.getParentTaskId(), dg.getParentCaseId()))
                .collect(Collectors.toList());
    }

    public List<DataGroup> getData() {
        return data;
    }
}
