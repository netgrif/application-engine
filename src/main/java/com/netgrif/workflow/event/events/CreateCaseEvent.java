package com.netgrif.workflow.event.events;

import com.netgrif.workflow.utils.DateUtils;
import com.netgrif.workflow.workflow.domain.Case;

public class CreateCaseEvent extends CaseEvent {

    public CreateCaseEvent(Case useCase) {
        super(useCase);
    }

    @Override
    public String getMessage() {
        return "Case " + getCase().getTitle() + " create on " + DateUtils.toString(time);
    }
}