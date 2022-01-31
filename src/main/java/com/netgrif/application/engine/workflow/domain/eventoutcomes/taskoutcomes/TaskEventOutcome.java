package com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CaseEventOutcome;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public abstract class TaskEventOutcome extends CaseEventOutcome {

    @Getter
    private Task task;

    protected TaskEventOutcome() {
    }

    protected TaskEventOutcome(Case aCase, Task task) {
        super(aCase);
        this.task = task;
    }

    protected TaskEventOutcome(I18nString message, Case aCase, Task task) {
        super(message, aCase);
        this.task = task;
    }

    protected TaskEventOutcome(I18nString message, List<EventOutcome> outcomes, Case aCase, Task task) {
        super(message, outcomes, aCase);
        this.task = task;
    }
}
