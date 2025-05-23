package com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.layoutoutcomes;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.layout.LayoutContainer;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Data;

import java.util.List;

@Data
public class GetLayoutsEventOutcome extends TaskEventOutcome {

    private LayoutContainer layout;

    public GetLayoutsEventOutcome(Case aCase, Task task) {
        super(aCase, task);
    }

    public GetLayoutsEventOutcome(I18nString message, Case aCase, Task task) {
        super(message, aCase, task);
    }

    public GetLayoutsEventOutcome(I18nString message, List<EventOutcome> outcomes, LayoutContainer container, Case aCase, Task task) {
        super(message, outcomes, aCase, task);
        this.layout = container;
    }
}
