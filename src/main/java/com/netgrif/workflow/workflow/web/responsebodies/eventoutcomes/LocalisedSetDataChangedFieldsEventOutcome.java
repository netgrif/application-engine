package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.SetDataChangedFieldsEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;
import lombok.Data;

import java.util.Locale;

public class LocalisedSetDataChangedFieldsEventOutcome extends LocalisedTaskEventOutcome {

    private ChangedFieldContainer data;

    public LocalisedSetDataChangedFieldsEventOutcome(SetDataChangedFieldsEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.data = outcome.getData();
    }

    public ChangedFieldContainer getData() {
        return data;
    }
}
