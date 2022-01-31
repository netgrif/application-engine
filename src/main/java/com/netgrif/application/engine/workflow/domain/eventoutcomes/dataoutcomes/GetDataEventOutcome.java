package com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;

import java.util.List;

public class GetDataEventOutcome extends TaskEventOutcome {

    private List<Field> data;

    public GetDataEventOutcome(Case aCase, Task task) {
        super(aCase, task);
    }

    public GetDataEventOutcome(I18nString message, Case aCase, Task task) {
        super(message, aCase, task);
    }

    public GetDataEventOutcome(I18nString message, List<EventOutcome> outcomes, Case aCase, Task task, List<Field> data) {
        super(message, outcomes, aCase, task);
        this.data = data;
    }

    public List<Field> getData() {
        return data;
    }

    public void setData(List<Field> data) {
        this.data = data;
    }
}
