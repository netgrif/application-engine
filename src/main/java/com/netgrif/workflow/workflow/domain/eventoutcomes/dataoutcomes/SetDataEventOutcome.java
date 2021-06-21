package com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.localised.LocalisedSetDataEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class SetDataEventOutcome extends EventOutcome {

    private ChangedFieldsTree data;

    @Override
    public LocalisedSetDataEventOutcome transformToLocalisedEventOutcome(Locale locale) {
        return new LocalisedSetDataEventOutcome(this, locale);
    }
}
