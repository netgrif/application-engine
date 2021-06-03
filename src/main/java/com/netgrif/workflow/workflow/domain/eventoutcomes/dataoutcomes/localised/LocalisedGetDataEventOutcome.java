package com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.localised;

import com.netgrif.workflow.workflow.domain.eventoutcomes.LocalisedEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedField;
import lombok.Data;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Data
public class LocalisedGetDataEventOutcome extends LocalisedEventOutcome {

    private List<LocalisedField> data;

    public LocalisedGetDataEventOutcome(GetDataEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.data = outcome.getData().stream()
                .map(field -> new LocalisedField(field, locale))
                .collect(Collectors.toList());
    }
}
