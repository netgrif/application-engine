package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.Locale;

public class LocalisedSetDataEventOutcome extends LocalisedTaskEventOutcome {

    private ChangedFieldContainer changedFields = new ChangedFieldContainer();

    public LocalisedSetDataEventOutcome(SetDataEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.changedFields.putAll(outcome.getChangedFields());
    }

    public ChangedFieldContainer getChangedFields() {
        return changedFields;
    }
}
