package com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.application.engine.workflow.domain.DataRef;
import com.netgrif.application.engine.workflow.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import lombok.Data;

import java.util.List;

@Data
public class GetDataEventOutcome extends TaskEventOutcome {

    private List<DataRef> data;

    public GetDataEventOutcome(Case aCase, Task task) {
        super(aCase, task);
    }

    public GetDataEventOutcome(I18nString message, Case aCase, Task task) {
        super(message, aCase, task);
    }

    public GetDataEventOutcome(I18nString message, List<EventOutcome> outcomes, Case aCase, Task task, List<DataRef> data) {
        super(message, outcomes, aCase, task);
        this.data = data;
    }
}
