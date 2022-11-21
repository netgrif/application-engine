package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.Locale;

public class LocalisedSetDataEventOutcome extends LocalisedTaskEventOutcome {

    private DataSet changedFields = new DataSet();

    public LocalisedSetDataEventOutcome(SetDataEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.changedFields = outcome.getChangedFields();
    }

    public DataSet getChangedFields() {
        return changedFields;
    }
}
