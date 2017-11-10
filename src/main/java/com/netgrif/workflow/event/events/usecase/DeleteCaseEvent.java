package com.netgrif.workflow.event.events.usecase;

import com.netgrif.workflow.utils.DateUtils;
import com.netgrif.workflow.workflow.domain.Case;

public class DeleteCaseEvent extends CaseEvent {

    public DeleteCaseEvent(Case useCase) {
        super(useCase);
    }

    @Override
    public String getMessage() {
        return "Case " + getCase().getTitle() + " deleted on " + DateUtils.toString(time);
    }
}