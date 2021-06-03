package com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.localised.LocalisedGetDataEventOutcome;
import lombok.Data;

import java.util.List;
import java.util.Locale;

@Data
public class GetDataEventOutcome extends EventOutcome {

    private List<Field> data;

    @Override
    public LocalisedGetDataEventOutcome transformToLocalisedEventOutcome(Locale locale) {
        return new LocalisedGetDataEventOutcome(this, locale);
    }
}
