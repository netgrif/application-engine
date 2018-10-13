package com.netgrif.workflow.event.events.usecase;

import com.netgrif.workflow.utils.DateUtils;
import com.netgrif.workflow.workflow.domain.Case;

public class CreateCaseEvent extends CaseEvent {

    public CreateCaseEvent(Case useCase) {
        super(useCase);
    }

    @Override
    public String getMessage() {
        return "Prípad " + getCase().getTitle() + " bol vytvorený " + DateUtils.toString(time);
    }
}