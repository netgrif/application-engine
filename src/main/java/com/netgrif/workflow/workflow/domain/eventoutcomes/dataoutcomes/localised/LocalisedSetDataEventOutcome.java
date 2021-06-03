package com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.localised;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree;
import com.netgrif.workflow.workflow.domain.eventoutcomes.LocalisedEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedSetDataEventOutcome extends LocalisedEventOutcome {

    private ChangedFieldsTree data;

    public LocalisedSetDataEventOutcome(SetDataEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.data = outcome.getData();
    }
}
