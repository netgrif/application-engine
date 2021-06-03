package com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.localised;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.workflow.domain.eventoutcomes.LocalisedEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.SetDataChangedFieldsEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedSetDataChangedFieldsEventOutcome extends LocalisedEventOutcome {

    private ChangedFieldContainer data;

    public LocalisedSetDataChangedFieldsEventOutcome(SetDataChangedFieldsEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.data = outcome.getData();
    }
}
