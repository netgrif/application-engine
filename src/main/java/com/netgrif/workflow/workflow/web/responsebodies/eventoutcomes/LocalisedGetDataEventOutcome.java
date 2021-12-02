package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedField;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LocalisedGetDataEventOutcome extends LocalisedTaskEventOutcome {

    private List<LocalisedField> data;

    public LocalisedGetDataEventOutcome(GetDataEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.data = outcome.getData().stream()
                .map(field -> new LocalisedField(field, locale))
                .collect(Collectors.toList());
    }

    public List<LocalisedField> getData() {
        return data;
    }
}
