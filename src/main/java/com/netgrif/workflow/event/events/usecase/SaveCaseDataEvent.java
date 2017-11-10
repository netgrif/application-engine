package com.netgrif.workflow.event.events.usecase;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.utils.DateUtils;
import com.netgrif.workflow.workflow.domain.Case;

import java.util.Collection;

public class SaveCaseDataEvent extends CaseEvent {

    private Collection<ChangedField> data;

    public SaveCaseDataEvent(Case useCase, Collection<ChangedField> data) {
        super(useCase);
        this.data = data;
    }

    @Override
    public String getMessage() {
        return "New data saved in case " + getCase().getTitle() + " on " + DateUtils.toString(time);
    }
}