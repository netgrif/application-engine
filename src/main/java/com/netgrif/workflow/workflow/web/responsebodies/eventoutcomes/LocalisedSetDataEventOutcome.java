package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.Locale;

public class LocalisedSetDataEventOutcome extends LocalisedTaskEventOutcome {

    private ChangedFieldContainer changedFields;

    public LocalisedSetDataEventOutcome(SetDataEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.changedFields = outcome.mergeChangedAndPropagated();
    }

    public ChangedFieldContainer getChangedFields() {
        return changedFields;
    }
}
