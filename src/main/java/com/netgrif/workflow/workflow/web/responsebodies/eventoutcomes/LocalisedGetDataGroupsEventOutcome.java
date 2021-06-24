package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.LocalisedEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.DataGroup;
import lombok.Data;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Data
public class LocalisedGetDataGroupsEventOutcome extends LocalisedEventOutcome {

    private List<DataGroup> data;

    public LocalisedGetDataGroupsEventOutcome(GetDataGroupsEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.data = outcome.getData().stream()
                .map(dg -> new DataGroup(dg.getFields(), dg.getTranslatedTitle(locale), dg.getAlignment(), dg.getStretch(), dg.getLayout()))
                .collect(Collectors.toList());
    }
}
